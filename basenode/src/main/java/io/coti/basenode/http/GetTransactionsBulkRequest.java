package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.util.List;

@Data
public class GetTransactionsBulkRequest extends GetBulkRequest {
    private List<Hash> hashes;

    public GetTransactionsBulkRequest() {
    }

    public GetTransactionsBulkRequest(List<Hash> hashes) {
        this.hashes = hashes;
    }
}
