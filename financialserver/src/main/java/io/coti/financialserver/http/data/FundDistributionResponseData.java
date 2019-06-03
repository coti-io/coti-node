package io.coti.financialserver.http.data;

import lombok.Data;

import java.util.List;

@Data
public class FundDistributionResponseData {

    private List<FundDistributionFileEntryResultData> fundDistributionResults;

    public FundDistributionResponseData(List<FundDistributionFileEntryResultData> fundDistributionResults) {
        this.fundDistributionResults = fundDistributionResults;
    }
}
