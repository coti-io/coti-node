package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;

import javax.validation.constraints.NotNull;

public class GetRollingReserveRequest extends Request {
    private static final long serialVersionUID = 3788649316889773495L;
    @NotNull
    public Hash userHash;

    @NotNull
    public double amount;
}
