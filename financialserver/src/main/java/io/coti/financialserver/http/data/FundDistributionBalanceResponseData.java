package io.coti.financialserver.http.data;

import io.coti.financialserver.http.FundDistributionBalanceResultData;
import lombok.Data;

import java.util.List;

@Data
public class FundDistributionBalanceResponseData {
    private List<FundDistributionBalanceResultData> fundDistributionBalanceResults;

    public FundDistributionBalanceResponseData(List<FundDistributionBalanceResultData> fundDistributionBalanceResults) {
        this.fundDistributionBalanceResults = fundDistributionBalanceResults;
    }
}
