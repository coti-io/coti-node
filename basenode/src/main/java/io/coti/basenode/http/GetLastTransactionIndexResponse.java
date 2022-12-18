package io.coti.basenode.http;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetLastTransactionIndexResponse extends BaseResponse {

    private long lastIndex;

    public GetLastTransactionIndexResponse(long lastIndex) {
        this.lastIndex = lastIndex;
    }
}
