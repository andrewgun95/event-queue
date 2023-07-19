package event;

import java.util.Set;

public interface EventQueue<T extends EventMessage> {

  void subscribe(EventHandler<T> eventHandler);

  void subscribe(Set<EventHandler<T>> eventHandlers);

  void unsubscribe(EventHandler<T> eventHandler);

  void dispatch(T message);

  Set<EventHandler<T>> getSubscribers();

}