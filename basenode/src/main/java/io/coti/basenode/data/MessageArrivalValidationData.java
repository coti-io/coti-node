package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
public class MessageArrivalValidationData<T extends DataHash> implements ISignable, ISignValidatable {

    private Map<String,Set<T>> classNameToHashes;

    // Signable
    private SignatureData signature;
    Hash signerHash;

    //TODO 3/24/2019 astolia: somehow mark the target of the original message

    public MessageArrivalValidationData(){
        classNameToHashes = new HashMap<>();
    }

    public MessageArrivalValidationData(Map<String,Set<T>> classNameToHashes){
        this.classNameToHashes = new HashMap<>();
        this.classNameToHashes.putAll(classNameToHashes);

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

    public void addHashesByNewKey(String className,Set<T> dataHashes) {
        classNameToHashes.put(className, dataHashes);
    }

}
