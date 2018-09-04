package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;

import javax.validation.constraints.NotNull;

public class GetTrustScoreRequest extends Request {
    @NotNull
    public Hash userHash;
}
