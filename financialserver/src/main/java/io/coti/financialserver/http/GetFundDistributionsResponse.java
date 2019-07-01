package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.FundDistributionResponseData;
import lombok.Data;

import java.util.List;

@Data
public class GetFundDistributionsResponse extends BaseResponse {

    private List<FundDistributionResponseData> fundDistributions;

    public GetFundDistributionsResponse(List<FundDistributionResponseData> fundDistributions) {
        this.fundDistributions = fundDistributions;
    }
}
