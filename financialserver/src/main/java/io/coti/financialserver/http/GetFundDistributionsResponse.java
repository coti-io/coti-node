package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.FundDistributionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetFundDistributionsResponse extends BaseResponse {

    private List<FundDistributionResponseData> fundDistributions;

    public GetFundDistributionsResponse(List<FundDistributionResponseData> fundDistributions) {
        this.fundDistributions = fundDistributions;
    }
}
