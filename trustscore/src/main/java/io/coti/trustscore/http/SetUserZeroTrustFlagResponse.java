package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.TrustScoreData;
import lombok.Data;

@Data
public class SetUserZeroTrustFlagResponse extends BaseResponse {
    private String userHash;
    private boolean zeroTrustFlag;

    public SetUserZeroTrustFlagResponse(TrustScoreData trustScoreData) {
        super();
        this.userHash = trustScoreData.getUserHash().toHexString();
        this.zeroTrustFlag = trustScoreData.getZeroTrustFlag();

    }
}
