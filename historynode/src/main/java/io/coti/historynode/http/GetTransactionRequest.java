package io.coti.historynode.http;


import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class GetTransactionRequest extends Request {
    @NotNull(message = "Transaction hash must not be blank")
    private Hash transactionHash;
}

