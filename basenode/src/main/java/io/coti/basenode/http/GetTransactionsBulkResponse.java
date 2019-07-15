package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.util.Map;

@Data
public class GetTransactionsBulkResponse extends BulkResponse {

    public Map<Hash, String> entitiesBulkResponses;

    public GetTransactionsBulkResponse() {
    }

    public GetTransactionsBulkResponse(Map<Hash, String> entitiesBulkResponses) {
        this.entitiesBulkResponses = entitiesBulkResponses;
    }
}
