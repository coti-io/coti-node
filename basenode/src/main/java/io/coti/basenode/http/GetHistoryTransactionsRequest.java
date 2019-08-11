package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class GetHistoryTransactionsRequest extends SerializableRequest {

    @NotEmpty
    private List<@Valid Hash> transactionHashes;

    public GetHistoryTransactionsRequest() {
    }

    public GetHistoryTransactionsRequest(List<Hash> transactionHashes) {
        this.transactionHashes = transactionHashes;
    }
}
