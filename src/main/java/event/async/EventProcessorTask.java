package event.async;

import event.EventHandler;
import event.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public class EventProcessorTask<T extends EventMessage> implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessorTask.class);

    private final Executor executor;

    private final ArrayBlockingQueue<Task> queueTasks;
    private final ShutdownCallback<T> shutdownCallback;

    private boolean running = false;
    private boolean shutdown = false;

    public EventProcessorTask(Executor executor, ShutdownCallback<T> shutdownCallback) {
        this(executor, 1, shutdownCallback);
    }

    private EventProcessorTask(Executor executor, int maxTasks, ShutdownCallback<T> shutdownCallback) {
        this.executor = executor;
        this.shutdownCallback = shutdownCallback;

        queueTasks = new ArrayBlockingQueue<>(maxTasks);
    }

    public void submit(T message, Set<EventHandler<T>> eventHandlers, BiConsumer<T, Set<EventHandler<T>>> processing) throws InterruptedException {
        if (shutdown) {
            LOGGER.warn("This task is shutdown, can't be submitted anymore");
        } else {
            // Submit task to queue and wait if the task is processing
            queueTasks.put(new Task(message, eventHandlers, processing));
            // Run this task if not running
            if (!running) {
                executor.execute(this);
                running = true;
            }
        }
    }

    @Override
    public void run() {
        while (running) { // main loop
            Task nextTask = queueTasks.peek();
            if (nextTask != null) {

                long beforeTime = System.currentTimeMillis();
                processTask(nextTask); // if failed can we resubmitted again ?
                long processTime = System.currentTimeMillis() - beforeTime;

                LOGGER.info("Task with id {} took {} secs to processed", Thread.currentThread().getId(), toSeconds(processTime));

                queueTasks.poll();
            } else {
                running = false;
                shutdown();
            }
        }
    }

    private double toSeconds(long time) {
        final double precision = 10000.0; // 4 zero after comma

        return Math.round((time / 1000.0) * precision) / precision;
    }

    private void shutdown() {
        shutdown = true;
        shutdownCallback.afterShutdown(this);
    }

    private void processTask(Task task) {
        BiConsumer<T, Set<EventHandler<T>>> processing = task.processing;
        processing.accept(task.message, task.eventHandlers);
    }

    private class Task {

        private final T message;
        private final Set<EventHandler<T>> eventHandlers;
        private final BiConsumer<T, Set<EventHandler<T>>> processing;

        private Task(T message, Set<EventHandler<T>> eventHandlers, BiConsumer<T, Set<EventHandler<T>>> processing) {
            this.message = message;
            this.eventHandlers = eventHandlers;
            this.processing = processing;
        }

    }

    @FunctionalInterface
    public interface ShutdownCallback<T extends EventMessage> {

        void afterShutdown(EventProcessorTask<T> task);

    }

}
