package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.data.MerchantRollingReserveData;
import io.coti.financialserver.data.RecourseClaimData;
import io.coti.financialserver.data.RollingReserveReleaseStatus;
import io.coti.financialserver.model.RollingReserveReleaseDates;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Data
public class GetRollingReserveReleaseDatesResponse extends BaseResponse {

    private String merchantHashString;
    private String rollingReserveAddress;
    private Map<String, RollingReserveReleaseStatus> rollingReserveReleases;
    private RecourseClaimData recourseClaimData;
    @Autowired
    private RollingReserveReleaseDates rollingReserveReleaseDates;

    public GetRollingReserveReleaseDatesResponse(MerchantRollingReserveData merchantRollingReserveData, Map<String, RollingReserveReleaseStatus> rollingReserveReleases, RecourseClaimData recourseClaimData) {
        super();

        this.rollingReserveReleases = rollingReserveReleases;
        Hash merchantHash = merchantRollingReserveData.getMerchantHash();
        merchantHashString = merchantHash.toString();
        rollingReserveAddress = merchantRollingReserveData.getRollingReserveAddress().toString();
        this.recourseClaimData = recourseClaimData;
    }
}
