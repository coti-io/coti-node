package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.util.List;

@Data
public class GetAddressesBulkRequest extends GetEntitiesBulkRequest{
    private List<Hash> hashes;

    public GetAddressesBulkRequest() {
    }

    public GetAddressesBulkRequest(List<Hash> hashes) {
        this.hashes = hashes;
    }
}
