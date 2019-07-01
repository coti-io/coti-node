package io.coti.financialserver.http.data;

import lombok.Data;

import java.util.List;

@Data
public class AddFundDistributionsResponseData {

    private List<FundDistributionFileEntryResultData> fundDistributionResults;

    public AddFundDistributionsResponseData(List<FundDistributionFileEntryResultData> fundDistributionResults) {
        this.fundDistributionResults = fundDistributionResults;
    }
}
