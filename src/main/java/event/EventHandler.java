package event;

public interface EventHandler<T extends EventMessage> {

    void handle(T message) throws EventHandlerException;

    boolean canHandle(T message);

}
