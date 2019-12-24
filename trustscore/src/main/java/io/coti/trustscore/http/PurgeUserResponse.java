package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class PurgeUserResponse extends BaseResponse {
    private String userHash;

    public PurgeUserResponse(Hash userHash) {
        super();
        this.userHash = userHash.toHexString();

    }
}
