package io.coti.basenode.http;

import io.coti.basenode.data.Hash;

import javax.validation.constraints.NotNull;

public class GetTransactionTrustScoreRequest extends Request {
    @NotNull
    public Hash userHash;
    @NotNull
    public Hash transactionHash;
}
