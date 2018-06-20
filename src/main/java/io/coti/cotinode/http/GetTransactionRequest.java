package io.coti.cotinode.http;

import io.coti.cotinode.data.Hash;

import javax.validation.constraints.NotNull;

public class GetTransactionRequest extends Request {
    @NotNull(message = "Addresses must not be blank")
    public Hash transactionHash;
}
