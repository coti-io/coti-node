package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;


//TODO 3/17/2019 astolia: Remove this comment:
// can take a look at DspVote for reference.


@Data
public class MessageArrivalValidationData implements IEntity, ISignable, ISignValidatable {

    Set<Hash> unvalidatedArrivalMessageHashes;
    private transient Hash hash;

    // Signable
    public SignatureData signature;
    Hash signerHash;

    public MessageArrivalValidationData(Hash hash){
        this.hash = hash;
        unvalidatedArrivalMessageHashes = new HashSet<>();
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
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

    public void addHashToUnvalidatedArrivalMessageHashes(Hash hash) {
        unvalidatedArrivalMessageHashes.add(hash);
    }

    public void removeHashFromUnvalidatedArrivalMessageHashes(Hash hash){
        unvalidatedArrivalMessageHashes.remove(hash);
    }

}
