package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class FundDistributionFileResultData implements IEntity, ISignable, ISignValidatable {

    private Hash financialServerHash;   // TODO: check this
    private List<byte[]> signatureMessage = new ArrayList<>();
    private int messageByteSize = 0;
    private SignatureData financialServerSignature;


    @Override
    public SignatureData getSignature() {
        return financialServerSignature;
    }

    @Override
    public Hash getSignerHash() {
        return financialServerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        financialServerHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        financialServerSignature = signature;
    }

    public void incrementMessageByteSize(long addedMessageByteSize) {
        messageByteSize += addedMessageByteSize;
    }

    @Override
    public Hash getHash() {
        return financialServerHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.financialServerHash = hash;
    }
}
