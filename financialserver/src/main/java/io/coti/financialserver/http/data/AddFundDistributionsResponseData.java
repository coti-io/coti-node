package io.coti.financialserver.http.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AddFundDistributionsResponseData implements Serializable {

    private List<FundDistributionFileEntryResultData> fundDistributionResults;

    public AddFundDistributionsResponseData(List<FundDistributionFileEntryResultData> fundDistributionResults) {
        this.fundDistributionResults = fundDistributionResults;
    }
}
