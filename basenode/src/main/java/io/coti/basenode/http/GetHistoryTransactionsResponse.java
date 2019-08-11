package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.util.Map;

@Data
public class GetHistoryTransactionsResponse extends BaseResponse {

    public Map<Hash, String> entitiesBulkResponses;

    public GetHistoryTransactionsResponse() {
    }

}
