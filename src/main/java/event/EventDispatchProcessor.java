package event;

import java.util.Set;
import java.util.function.BiConsumer;

public interface EventDispatchProcessor<T extends EventMessage> {

    void process(T message, Set<EventHandler<T>> eventHandlers, BiConsumer<T, Set<EventHandler<T>>> processing);

}
