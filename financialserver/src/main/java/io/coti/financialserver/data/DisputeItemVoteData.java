package io.coti.financialserver.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public class DisputeItemVoteData implements IEntity, Serializable, ISignable, ISignValidatable {

    private Hash hash;
    private Hash userHash;
    @NotNull
    private Long itemId;
    @NotNull
    private Hash disputeHash;
    @NotNull
    private DisputeItemStatus status;
    private SignatureData userSignature;

    public void init() {
        byte[] concatDisputeHashAndItemIdBytes = ArrayUtils.addAll(disputeHash.getBytes(),itemId.toString().getBytes());
        hash = CryptoHelper.cryptoHash( concatDisputeHashAndItemIdBytes );
    }

    @Override
    public Hash getHash() {
        return hash;
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
