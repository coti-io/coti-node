package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;

@Data
public class GetTransactionRequest extends Request {

    @Valid
    private Hash transactionHash;

    public GetTransactionRequest() {
    }

    public GetTransactionRequest(Hash transactionHash) {
        this.transactionHash = transactionHash;
    }
}
