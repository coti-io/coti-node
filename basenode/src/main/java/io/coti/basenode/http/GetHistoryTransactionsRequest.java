package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.util.List;

@Data
public class GetHistoryTransactionsRequest extends SeriazableRequest {
    private List<Hash> hashes;

    public GetHistoryTransactionsRequest() {
    }

    public GetHistoryTransactionsRequest(List<Hash> hashes) {
        this.hashes = hashes;
    }
}
