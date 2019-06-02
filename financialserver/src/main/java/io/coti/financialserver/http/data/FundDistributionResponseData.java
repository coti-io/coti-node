package io.coti.financialserver.http.data;

import io.coti.financialserver.http.FundDistributionFileEntryResultData;
import lombok.Data;

import java.util.List;

@Data
public class FundDistributionResponseData {

    private List<FundDistributionFileEntryResultData> fundDistributionResults;

    public FundDistributionResponseData(List<FundDistributionFileEntryResultData> fundDistributionResults) {
        this.fundDistributionResults = fundDistributionResults;
    }
}
