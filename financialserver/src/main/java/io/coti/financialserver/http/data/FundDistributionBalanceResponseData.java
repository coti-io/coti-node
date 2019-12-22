package io.coti.financialserver.http.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FundDistributionBalanceResponseData implements Serializable {
    private List<FundDistributionBalanceResultData> fundDistributionBalanceResults;

    public FundDistributionBalanceResponseData(List<FundDistributionBalanceResultData> fundDistributionBalanceResults) {
        this.fundDistributionBalanceResults = fundDistributionBalanceResults;
    }
}
