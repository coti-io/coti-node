package io.coti.financialserver.http.data;

import io.coti.financialserver.http.FundDistributionFileEntryResultData;
import lombok.Data;

import java.util.List;

@Data
public class FundDistributionResponseData {

    private List<FundDistributionFileEntryResultData> fundDistributionFileEntryResults;

    public FundDistributionResponseData(List<FundDistributionFileEntryResultData> fundDistributionFileEntryResults) {
        this.fundDistributionFileEntryResults = fundDistributionFileEntryResults;
    }
}
