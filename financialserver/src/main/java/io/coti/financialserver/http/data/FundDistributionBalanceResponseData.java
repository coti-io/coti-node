package io.coti.financialserver.http.data;

import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.util.List;

@Data
public class FundDistributionBalanceResponseData implements IResponseData {

    private List<FundDistributionBalanceResultData> fundDistributionBalanceResults;

    public FundDistributionBalanceResponseData(List<FundDistributionBalanceResultData> fundDistributionBalanceResults) {
        this.fundDistributionBalanceResults = fundDistributionBalanceResults;
    }
}
