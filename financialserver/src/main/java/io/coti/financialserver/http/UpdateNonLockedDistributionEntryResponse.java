package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.FundDistributionResponseStateData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateNonLockedDistributionEntryResponse extends BaseResponse {

    private FundDistributionResponseStateData fundDistribution;

    public UpdateNonLockedDistributionEntryResponse(FundDistributionResponseStateData fundDistribution) {
        this.fundDistribution = fundDistribution;
    }
}
