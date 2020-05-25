package io.coti.fullnode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetTransactionRequest implements IRequest {

    @NotNull(message = "Transaction hash must not be blank")
    private @Valid Hash transactionHash;
}
