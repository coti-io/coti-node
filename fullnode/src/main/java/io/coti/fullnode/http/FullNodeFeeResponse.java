package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.fullnode.http.data.FullNodeFeeResponseData;
import lombok.Data;

@Data
public class FullNodeFeeResponse extends BaseResponse {

    private FullNodeFeeResponseData fullNodeFee;

    public FullNodeFeeResponse(FullNodeFeeResponseData fullNodeFeeResponseData) {
        super();
        this.fullNodeFee = fullNodeFeeResponseData;
    }
}
