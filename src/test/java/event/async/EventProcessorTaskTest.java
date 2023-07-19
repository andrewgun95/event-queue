package event.async;

import event.EventHandler;
import event.EventHandlerException;
import event.NumberMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class EventProcessorTaskTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessorTaskTest.class);

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Test
    public void testSubmit() {
        EventProcessorTask.ShutdownCallback<NumberMessage> shutdownCallback = task -> {
            LOGGER.info("Shutting down task " + task.toString());
        };

        EventHandler<NumberMessage> numberHandler1 = new EventHandler<>() {
            @Override
            public void handle(NumberMessage message) throws EventHandlerException {
                message.multiple(3);
                message.add(1);
            }

            @Override
            public boolean canHandle(NumberMessage message) {
                return message.isActive();
            }

        };

        EventHandler<NumberMessage> numberHandler2 = new EventHandler<>() {
            @Override
            public void handle(NumberMessage message) throws EventHandlerException {
                message.add(1);
                message.multiple(3);
            }

            @Override
            public boolean canHandle(NumberMessage message) {
                return message.isActive();
            }

        };


        try {
            NumberMessage number1 = new NumberMessage(2);

            EventProcessorTask<NumberMessage> task1 = new EventProcessorTask<>(executor, shutdownCallback);
            task1.submit(number1, Collections.singleton(numberHandler1), this::testDispatcher);
            task1.submit(number1, Collections.singleton(numberHandler2), this::testDispatcher);

            Thread.sleep(1000);

            Integer result = (Integer) number1.getPayload();
            Assertions.assertEquals(24, result);

        } catch (InterruptedException ignored) {
        }
    }

    public void testDispatcher(NumberMessage numberMessage, Set<EventHandler<NumberMessage>> numberHandlers) {
        try {
            for (EventHandler<NumberMessage> eventHandler : numberHandlers) {
                if (eventHandler.canHandle(numberMessage)) {
                    eventHandler.handle(numberMessage);
                }
            }
        } catch (EventHandlerException e) {
            LOGGER.error(e.getMessage(), e.getCause());
        }
    }

}
