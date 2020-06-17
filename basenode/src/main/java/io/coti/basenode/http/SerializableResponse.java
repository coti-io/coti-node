package io.coti.basenode.http;

import io.coti.basenode.http.interfaces.ISerializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SerializableResponse extends Response implements ISerializable {

    private SerializableResponse() {
    }

    public SerializableResponse(String message, String status) {
        super(message, status);
    }

    public SerializableResponse(String message) {
        super(message);
    }
}
