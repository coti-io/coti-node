package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetHistoryTransactionsResponse extends BaseResponse {

    private Map<Hash, String> entitiesBulkResponses;

}
