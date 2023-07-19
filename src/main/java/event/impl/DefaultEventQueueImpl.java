package event.impl;

import event.AbstractEventQueue;
import event.EventHandler;
import event.EventMessage;
import event.async.AsyncEventDispatchProcessor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

public class DefaultEventQueueImpl<T extends EventMessage> extends AbstractEventQueue<T> {

    private final Set<EventHandler<T>> subscribers;

    public DefaultEventQueueImpl(Executor executor, boolean async) {
        super(async ? new AsyncEventDispatchProcessor<>(executor) : null);
        subscribers = new HashSet<>();
    }

    public DefaultEventQueueImpl(Executor executor, int unitOfHandlers, boolean async) {
        super(async ? new AsyncEventDispatchProcessor<>(executor, unitOfHandlers) : null);
        subscribers = new HashSet<>();
    }

    @Override
    public void subscribe(EventHandler<T> eventHandler) {
        subscribers.add(eventHandler);
    }

    @Override
    public void subscribe(Set<EventHandler<T>> eventHandlers) {
        subscribers.addAll(eventHandlers);
    }

    @Override
    public void unsubscribe(EventHandler<T> eventHandler) {
        subscribers.remove(eventHandler);
    }

    @Override
    public Set<EventHandler<T>> getSubscribers() {
        return subscribers;
    }
}
