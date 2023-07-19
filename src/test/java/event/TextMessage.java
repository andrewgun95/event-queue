package event;

import java.util.Map;
import java.util.TreeMap;

public class TextMessage implements EventMessage {

    private final String text;

    public TextMessage(String text) {
        this.text = text;
    }

    @Override
    public Object getPayload() {
        return text;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return new TreeMap<>();
    }

}
