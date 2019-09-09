package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;

import javax.validation.constraints.NotNull;

public class GetNetworkFeeRequest extends Request {
    private static final long serialVersionUID = 237904147312039820L;
    @NotNull
    public Hash userHash;

    @NotNull
    public double amount;
}