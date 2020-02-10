package io.coti.financialserver.http.data;

import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.util.List;

@Data
public class AddFundDistributionsResponseData implements IResponseData {

    private List<FundDistributionFileEntryResultData> fundDistributionResults;

    public AddFundDistributionsResponseData(List<FundDistributionFileEntryResultData> fundDistributionResults) {
        this.fundDistributionResults = fundDistributionResults;
    }
}
