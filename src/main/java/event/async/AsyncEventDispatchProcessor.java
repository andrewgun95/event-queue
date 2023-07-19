package event.async;

import event.EventDispatchProcessor;
import event.EventHandler;
import event.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AsyncEventDispatchProcessor<T extends EventMessage> implements EventDispatchProcessor<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncEventDispatchProcessor.class);

    private final Executor executor;

    private final Map<EventProcessorTask<T>, Set<EventHandler<T>>> tasks;

    private final int unitOfHandlers;

    public AsyncEventDispatchProcessor(Executor executor) {
        this(executor, 100);
    }

    public AsyncEventDispatchProcessor(Executor executor, int unitOfHandlers) {
        this.executor = executor;
        this.unitOfHandlers = unitOfHandlers;

        tasks = new ConcurrentHashMap<>();
    }

    @Override
    public void process(T message, Set<EventHandler<T>> eventHandlers, BiConsumer<T, Set<EventHandler<T>>> processing) {
        // Prepare tasks from event handlers
        Map<EventProcessorTask<T>, Set<EventHandler<T>>> newTasks = describeTasks(eventHandlers, unitOfHandlers);
        // Wait until all tasks is finished executed
        synchronized (this) {
            while (!tasks.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e.getCause());
                }
            }
        }
        // Execute a new tasks
        tasks.putAll(newTasks);
        tasks.forEach((task, taskEventHandlers) -> {
            try {
                task.submit(message, taskEventHandlers, processing);
            } catch (InterruptedException ignored) {
            }
        });
    }

    private synchronized void afterShutdown(EventProcessorTask<T> processorTask) {
        tasks.remove(processorTask);
        notify();
    }

    /**
     * Describe many of tasks need to be processing from a given event handlers. i.e,
     * Event Queue which have 100 event handlers will be processed on 3 task of processing unit :
     * <pre>
     *    From 0  to 40  unit will process on Task 1
     *    From 41 to 80  unit will process on Task 2
     *    From 81 to 100 unit will process on Task 3
     * </pre>
     * (per unit equal to 40 handlers)
     *
     * @param eventHandlers  event handlers
     * @param unitOfHandlers smallest unit of handlers for each task
     */
    private Map<EventProcessorTask<T>, Set<EventHandler<T>>> describeTasks(Set<EventHandler<T>> eventHandlers, int unitOfHandlers) {
        if (eventHandlers.size() <= unitOfHandlers) {
            Map<EventProcessorTask<T>, Set<EventHandler<T>>> result = new HashMap<>();
            result.put(new EventProcessorTask<>(executor, this::afterShutdown), eventHandlers);
            return result;
        } else {
            // Reference is indexes dictionary for event handlers
            List<EventHandler<T>> reference = new ArrayList<>(eventHandlers);

            Map<Boolean, Set<EventHandler<T>>> results = eventHandlers.parallelStream().collect(Collectors.partitioningBy(ele -> {
                // Get index from reference
                int index = reference.indexOf(ele);
                return index < unitOfHandlers;
            }, Collectors.toSet()));

            Set<EventHandler<T>> proceedEventHandlers = results.get(true);
            Set<EventHandler<T>> remainsEventHandlers = results.get(false);

            Map<EventProcessorTask<T>, Set<EventHandler<T>>> result = describeTasks(remainsEventHandlers, unitOfHandlers);
            result.put(new EventProcessorTask<>(executor, this::afterShutdown), proceedEventHandlers);
            return result;
        }
    }

    public Set<EventProcessorTask<T>> getTasks() {
        return tasks.keySet();
    }

}
