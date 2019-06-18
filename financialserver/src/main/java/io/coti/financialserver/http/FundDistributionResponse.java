package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.FundDistributionResponseData;
import lombok.Data;

@Data
public class FundDistributionResponse extends BaseResponse {

    private FundDistributionResponseData fundDistributionResponseData;

    public FundDistributionResponse(FundDistributionResponseData fundDistributionResponseData) {
        this.fundDistributionResponseData = fundDistributionResponseData;
    }
}
