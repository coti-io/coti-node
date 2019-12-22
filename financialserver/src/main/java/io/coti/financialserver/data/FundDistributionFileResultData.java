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

    private static final long serialVersionUID = -4032461223979261902L;
    private Hash financialServerHash;
    private List<byte[]> signatureMessage = new ArrayList<>();
    private int messageByteSize = 0;
    private SignatureData financialServerSignature;


    @Override
    public SignatureData getSignature() {
        return financialServerSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        financialServerSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return financialServerHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        financialServerHash = signerHash;
    }

    public void incrementMessageByteSize(long addedMessageByteSize) {
        messageByteSize += addedMessageByteSize;
    }

    @Override
    public Hash getHash() {
        return getSignerHash();
    }

    @Override
    public void setHash(Hash hash) {
        this.financialServerHash = hash;
    }
}
