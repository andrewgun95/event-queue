package event;

import java.util.Collections;
import java.util.Map;

public class NumberMessage implements EventMessage {

    private Integer number;

    private boolean active;

    public NumberMessage(Integer number) {
        this.number = number;
        this.active = true;
    }

    public void add(Integer amount) {
        this.number += amount;
    }

    public void multiple(Integer amount) {
        this.number *= amount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public Object getPayload() {
        return number;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return Collections.emptyMap();
    }

}
