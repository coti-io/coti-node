package io.coti.storagenode.http;

import io.coti.basenode.http.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddEntityJsonResponse extends Response {
    private String details;

    public AddEntityJsonResponse(String status, String message, String details) {
        super(message);
        this.status = status;
        this.details = details;
    }
}
