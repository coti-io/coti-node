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
import java.util.Date;
import java.util.List;

@Data
public class DisputeDocumentData implements IEntity, ISignable, ISignValidatable {

    private Hash hash;
    @NotNull
    private Hash userHash;
    @NotNull
    private List<Long> itemIds;
    @NotNull
    private SignatureData userSignature;
    @NotNull
    private Hash disputeHash;
    private ActionSide uploadSide;
    private String name;
    private String description;
    private String fileName;
    private Date creationTime;

    /**
     * why this is private?
     */
    /*private DisputeDocumentData() {

    }*/

    public void init() {
        this.creationTime = new Date();
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
