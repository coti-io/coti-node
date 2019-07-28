package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.util.Map;

@Data
public class GetHistoryTransactionsResponse extends Response {

    public Map<Hash, String> entitiesBulkResponses;

    public GetHistoryTransactionsResponse() {
    }

    public GetHistoryTransactionsResponse(Map<Hash, String> entitiesBulkResponses) {
        this.entitiesBulkResponses = entitiesBulkResponses;
    }

    public GetHistoryTransactionsResponse(Map<Hash, String> entitiesBulkResponses, String message, String status) {
        super(message, status);
        this.entitiesBulkResponses = entitiesBulkResponses;
    }

}
