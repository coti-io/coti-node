package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;

import javax.validation.constraints.NotNull;

public class GetTransactionRequest extends Request {
    @NotNull(message = "Addresses must not be blank")
    public Hash transactionHash;
}
