package event;

@FunctionalInterface
public interface EventDispatchInterceptor<T extends EventMessage> {

    T intercept(T message);

}
