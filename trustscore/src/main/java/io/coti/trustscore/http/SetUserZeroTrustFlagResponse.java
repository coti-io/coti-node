package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.UserTrustScoreData;
import lombok.Data;

@Data
public class SetUserZeroTrustFlagResponse extends BaseResponse {
    private static final long serialVersionUID = -3647736768164695318L;
    private String userHash;
    private boolean zeroTrustFlag;

    public SetUserZeroTrustFlagResponse(UserTrustScoreData userTrustScoreData) {
        super();
        this.userHash = userTrustScoreData.getHash().toHexString();
        this.zeroTrustFlag = userTrustScoreData.getZeroTrustFlag();

    }
}
