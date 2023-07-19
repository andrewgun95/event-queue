package event;

import event.impl.DefaultEventQueueImpl;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventQueueTest {

    @Test
    public void testImplementation() {
        Executor executor = Executors.newSingleThreadExecutor();

        EventQueue<TextMessage> eventQueue = new DefaultEventQueueImpl<>(executor, 40, false);

        Set<EventHandler<TextMessage>> textHandlers = new HashSet<>();

        for (int i = 1; i <= 100; i++) {
            final Integer number = i;
            textHandlers.add(new EventHandler<>() {
                @Override
                public void handle(TextMessage message) throws EventHandlerException {
                    System.out.println("Test " + number + " : " + message.getPayload());
                }

                @Override
                public boolean canHandle(TextMessage message) {
                    String text = (String) message.getPayload();
                    return text != null && !text.trim().isEmpty();
                }

            });
        }
        eventQueue.subscribe(textHandlers);

        eventQueue.dispatch(new TextMessage("Hello, World!"));
    }

}
