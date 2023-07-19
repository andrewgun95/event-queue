package event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class AbstractEventQueue<T extends EventMessage> implements EventQueue<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEventQueue.class);

    private Set<EventDispatchInterceptor<T>> dispatchInterceptors;

    private EventDispatchProcessor<T> dispatchProcessor;

    protected AbstractEventQueue() {
        this(null);
    }

    protected AbstractEventQueue(EventDispatchProcessor<T> dispatchProcessor) {
        dispatchInterceptors = new CopyOnWriteArraySet<>(); // safe thread

        this.dispatchProcessor = dispatchProcessor;
    }

    public void setDispatchInterceptors(Set<EventDispatchInterceptor<T>> dispatchInterceptors) {
        this.dispatchInterceptors = dispatchInterceptors;
    }

    public void setDispatchProcessor(EventDispatchProcessor<T> dispatchProcessor) {
        this.dispatchProcessor = dispatchProcessor;
    }

    public Set<EventDispatchInterceptor<T>> getDispatchInterceptors() {
        return dispatchInterceptors;
    }

    public EventDispatchProcessor<T> getDispatchProcessor() {
        return dispatchProcessor;
    }

    public void addInterceptor(EventDispatchInterceptor<T> dispatchInterceptor) {
        dispatchInterceptors.add(dispatchInterceptor);
    }

    public void removeInterceptor(EventDispatchInterceptor<T> dispatchInterceptor) {
        if (dispatchInterceptors.contains(dispatchInterceptor)) {
            dispatchInterceptors.remove(dispatchInterceptor);
        } else {
            LOGGER.warn("Can't found any given event dispatch interceptor in the sets");
        }
    }

    @Override
    public void dispatch(T message) {
        // Retrieve dispatched message
        T dispatchMessage = message;
        for (EventDispatchInterceptor<T> interceptor : dispatchInterceptors) { // intercept dispatched message
            dispatchMessage = interceptor.intercept(dispatchMessage);
        }
        // Retrieve event handlers from subscribers
        Set<EventHandler<T>> eventHandlers = getSubscribers();
        if (dispatchProcessor != null) {
            dispatchProcessor.process(dispatchMessage, eventHandlers, this::messageDispatcher);
        } else {
            messageDispatcher(dispatchMessage, eventHandlers);
        }
    }

    private void messageDispatcher(T dispatchMessage, Set<EventHandler<T>> eventHandlers) {
        try {
            for (EventHandler<T> handler : eventHandlers) {
                if (handler.canHandle(dispatchMessage)) {
                    handler.handle(dispatchMessage);
                }
            }
        } catch (EventHandlerException e) {
            LOGGER.error(e.getMessage(), e.getCause());
        }
    }

}
