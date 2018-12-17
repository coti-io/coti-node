package io.coti.financialserver.data;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;

@Data
public class ReceiverBaseTransactionOwnerData implements IEntity, ISignable, ISignValidatable {
    private Hash merchantHash;
    private Hash receiverBaseTransactionHash;
    private SignatureData merchantSignature;

    private ReceiverBaseTransactionOwnerData() {

    }

    public ReceiverBaseTransactionOwnerData(Hash merchantHash, Hash receiverBaseTransactionHash, SignatureData merchantSignature){
        this.merchantHash = merchantHash;
        this.receiverBaseTransactionHash = receiverBaseTransactionHash;
        this.merchantSignature = merchantSignature;
    }

    @Override
    public Hash getHash() {
        return receiverBaseTransactionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.receiverBaseTransactionHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return merchantSignature;
    }

    @Override
    public Hash getSignerHash() {
        return merchantHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        merchantHash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.merchantSignature = signature;
    }
}
