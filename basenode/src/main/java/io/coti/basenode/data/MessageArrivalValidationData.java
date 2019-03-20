package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;


//TODO 3/17/2019 astolia: Remove this comment:
// can take a look at DspVote for reference.


@Data
public class MessageArrivalValidationData implements ISignable, ISignValidatable {

    //TODO 3/18/2019 astolia: Add ArrivalValidationType

    Set<TransactionDataHash> transactionHashes;
    Set<AddressDataHash> addressHashes;

//    private transient Hash hash;

    // Signable
    public SignatureData signature;
    Hash signerHash;

    public MessageArrivalValidationData(/*Hash hash*/){
        transactionHashes = new HashSet<>();
        addressHashes = new HashSet<>();
    }

    //TODO 3/18/2019 astolia: Not sure if why and who should be Hash.
    public MessageArrivalValidationData(/*Hash hash, */Set<TransactionDataHash> transactionHashes, Set<AddressDataHash> addressHashes){
        this.transactionHashes = transactionHashes;
        this.addressHashes = addressHashes;
    }

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return signerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.signerHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }

    public void addHashToTransactionHashes(Hash hash) {
        transactionHashes.add(new TransactionDataHash(hash));
    }

    public void removeHashFromTransactionHashes(Hash hash){
        transactionHashes.remove(new TransactionDataHash(hash));
    }

    public void addHashToAddressHashes(Hash hash) {
        addressHashes.add(new AddressDataHash(hash));
    }

    public void removeHashFroAddressHashes(Hash hash){
        addressHashes.remove(new AddressDataHash(hash));
    }

//    @Override
//    public String toString(){
//
//    }
}
