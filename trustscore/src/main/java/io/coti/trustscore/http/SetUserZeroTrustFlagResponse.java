package io.coti.trustscore.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class SetUserZeroTrustFlagResponse extends BaseResponse {
    private String userHash;
    private boolean zeroTrustFlag;

    public SetUserZeroTrustFlagResponse(SetUserZeroTrustFlagRequest request) {
        super();
        this.userHash = request.getUserHash().toHexString();
        this.zeroTrustFlag = request.isZeroTrustFlag();

    }
}
