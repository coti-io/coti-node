package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.FundDistributionResponseStateData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetNonLockedDistributionEntriesResponse extends BaseResponse {

    private List<FundDistributionResponseStateData> fundDistributions;

    public GetNonLockedDistributionEntriesResponse(List<FundDistributionResponseStateData> fundDistributions) {
        this.fundDistributions = fundDistributions;
    }
}
