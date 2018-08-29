package io.coti.basenode.http;

import io.coti.basenode.data.Hash;

import javax.validation.constraints.NotNull;

public class GetTransactionRequest extends Request {
    @NotNull(message = "Addresses must not be blank")
    public Hash transactionHash;
}
