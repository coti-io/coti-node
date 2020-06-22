package io.coti.fullnode.http;

import io.coti.basenode.http.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddTransactionResponse extends Response {

    private Instant attachmentTime;

    public AddTransactionResponse(String message, Instant attachmentTime) {
        super(message);
        this.attachmentTime = attachmentTime;
    }
}