package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

@Data
public class GetHashToTransactionResponse implements IPropagatable {
    private TransactionData transactionData;
    private Hash hash;

    public GetHashToTransactionResponse(){
    }

    public GetHashToTransactionResponse(Hash hash, TransactionData transactionData){
        this.hash = hash;
        this.transactionData = transactionData;
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }
}
