package io.coti.basenode.http;

import io.coti.basenode.http.interfaces.ISerializable;
import lombok.Data;

@Data
public class SeriazableResponse extends Response implements ISerializable {

    private SeriazableResponse() {
    }

    public SeriazableResponse(String message, String status) {
        super(message, status);
    }
}
