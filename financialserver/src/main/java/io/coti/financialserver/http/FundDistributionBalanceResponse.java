package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.FundDistributionBalanceResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FundDistributionBalanceResponse extends BaseResponse {

    private FundDistributionBalanceResponseData fundDistributionBalanceResponseData;

    public FundDistributionBalanceResponse(FundDistributionBalanceResponseData fundDistributionBalanceResponseData) {
        super();
        this.fundDistributionBalanceResponseData = fundDistributionBalanceResponseData;
    }
}
