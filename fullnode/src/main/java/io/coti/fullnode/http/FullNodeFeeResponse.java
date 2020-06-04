package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.fullnode.http.data.FullNodeFeeResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FullNodeFeeResponse extends BaseResponse {

    private FullNodeFeeResponseData fullNodeFee;

    public FullNodeFeeResponse(FullNodeFeeResponseData fullNodeFeeResponseData) {
        this.fullNodeFee = fullNodeFeeResponseData;
    }
}
