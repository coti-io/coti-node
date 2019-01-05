package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Data
public class DisputeCommentData implements IEntity, ISignable, ISignValidatable {

    private Hash hash;
    @NotNull
    private Hash userHash;
    @NotNull
    private List<Long> itemIds;
    @NotNull
    private Hash disputeHash;
    @NotNull
    private SignatureData userSignature;
    private ActionSide commentSide;
    @NotNull
    private String comment;
    private Instant creationTime;

    private DisputeCommentData() {

    }

    public void init() {
        this.creationTime = Instant.now();
        byte[] concatDateAndUserHashBytes = ArrayUtils.addAll(userHash.getBytes(), creationTime.toString().getBytes());
        this.hash = CryptoHelper.cryptoHash(concatDateAndUserHashBytes);
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
        return userSignature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        userHash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.userSignature = signature;
    }
}
