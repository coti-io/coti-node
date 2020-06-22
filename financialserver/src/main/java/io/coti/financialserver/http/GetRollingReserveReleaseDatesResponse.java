package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.MerchantRollingReserveData;
import io.coti.financialserver.data.RecourseClaimData;
import io.coti.financialserver.data.RollingReserveReleaseStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetRollingReserveReleaseDatesResponse extends BaseResponse {

    private String merchantHashString;
    private String rollingReserveAddress;
    private Map<String, RollingReserveReleaseStatus> rollingReserveReleases;
    private RecourseClaimData recourseClaimData;

    public GetRollingReserveReleaseDatesResponse(MerchantRollingReserveData merchantRollingReserveData, Map<String, RollingReserveReleaseStatus> rollingReserveReleases, RecourseClaimData recourseClaimData) {

        this.rollingReserveReleases = rollingReserveReleases;
        Hash merchantHash = merchantRollingReserveData.getMerchantHash();
        merchantHashString = merchantHash.toString();
        rollingReserveAddress = merchantRollingReserveData.getRollingReserveAddress().toString();
        this.recourseClaimData = recourseClaimData;
    }
}
