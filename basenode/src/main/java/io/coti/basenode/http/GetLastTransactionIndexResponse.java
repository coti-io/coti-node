package io.coti.basenode.http;

import lombok.Data;

@Data
public class GetLastTransactionIndexResponse extends BaseResponse {

    private long lastIndex;

    public GetLastTransactionIndexResponse(long lastIndex) {
        this.lastIndex = lastIndex;
    }
}
