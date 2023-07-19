package event;

public class EventHandlerException extends Exception {

    public EventHandlerException(String message) {
        super(message);
    }

    public EventHandlerException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
