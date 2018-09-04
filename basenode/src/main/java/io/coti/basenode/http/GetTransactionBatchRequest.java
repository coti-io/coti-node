package io.coti.basenode.http;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class GetTransactionBatchRequest implements Serializable {
    @NotNull
    public long startingIndex;

    private GetTransactionBatchRequest() {
    }

    public GetTransactionBatchRequest(long startingIndex) {
        this.startingIndex = startingIndex;
    }
}
