package event;

import java.io.Serializable;
import java.util.Map;


public interface EventMessage extends Serializable {

    Object getPayload();

    Map<String, Object> getMetadata();

}
