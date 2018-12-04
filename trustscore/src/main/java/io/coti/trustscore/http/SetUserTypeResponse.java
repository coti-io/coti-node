package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.Enums.UserType;
import lombok.Data;

@Data
public class SetUserTypeResponse extends BaseResponse {
    private String userType;
    private String userHash;

    public SetUserTypeResponse(UserType userType, Hash userHash) {
        super();
        this.userType = userType.toString();
        this.userHash = userHash.toHexString();
    }
}
