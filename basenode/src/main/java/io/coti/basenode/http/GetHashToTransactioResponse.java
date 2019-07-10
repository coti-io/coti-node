package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IPropagatable;

public class GetHashToTransactioResponse implements IPropagatable {
    private TransactionData transactionData;
    private Hash hash;

    public GetHashToTransactioResponse(){
    }

    public GetHashToTransactioResponse(Hash hash, TransactionData transactionData){
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
