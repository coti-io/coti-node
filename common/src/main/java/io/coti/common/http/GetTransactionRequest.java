package io.coti.common.http;

import io.coti.common.data.Hash;

import javax.validation.constraints.NotNull;

public class GetTransactionRequest extends Request {
    @NotNull(message = "Addresses must not be blank")
    public Hash transactionHash;
}
