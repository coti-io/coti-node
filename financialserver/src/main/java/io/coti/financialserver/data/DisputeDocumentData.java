package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.financialserver.data.interfaces.IDisputeEvent;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.time.Instant;
import java.util.List;

@Data
public class DisputeDocumentData implements IDisputeEvent, IEntity, ISignable, ISignValidatable {

    private static final long serialVersionUID = 8094265736309125215L;
    private Hash hash;
    private Hash userHash;
    private List<Long> itemIds;
    private SignatureData userSignature;
    private Hash disputeHash;
    private ActionSide uploadSide;
    private String description;
    private String fileName;
    private Instant creationTime;

    public DisputeDocumentData(Hash userHash, Hash disputeHash, List<Long> itemIds, SignatureData userSignature) {
        this.userHash = userHash;
        this.disputeHash = disputeHash;
        this.itemIds = itemIds;
        this.userSignature = userSignature;
        init();
    }

    public void init() {
        this.creationTime = Instant.now();
        byte[] concatDateAndUserHashBytes = ArrayUtils.addAll(userHash.getBytes(), creationTime.toString().getBytes());
        this.hash = CryptoHelper.cryptoHash(concatDateAndUserHashBytes);
    }

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.userSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        userHash = hash;
    }
}
