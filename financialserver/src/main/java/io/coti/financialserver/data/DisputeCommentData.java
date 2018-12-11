package io.coti.financialserver.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import java.util.Date;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ArrayUtils;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;


@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public class DisputeCommentData implements IEntity, ISignable, ISignValidatable {

    @NotNull
    private Hash userHash;

    @NotNull
    private Long itemId;

    @NotNull
    private Hash disputeHash;

    @NotNull
    private SignatureData userSignature;

    private Hash hash;
    private ActionSide commentSide;
    private String comment;
    private Date creationTime;

    public void init() {
        this.creationTime = new Date();
        byte[] concatDateAndUserHashBytes = ArrayUtils.addAll(userHash.getBytes(),creationTime.toString().getBytes());
        this.hash = CryptoHelper.cryptoHash( concatDateAndUserHashBytes );
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
