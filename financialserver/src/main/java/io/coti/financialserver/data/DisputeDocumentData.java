package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Date;

@Data
public class DisputeDocumentData implements IEntity {
    private Hash hash;
    private ActionSide uploadSide;
    private String name;
    private String description;
    private Date creationTime;

    public DisputeDocumentData(Hash userHash, ActionSide uploadSide, String name, String description) {
        this.uploadSide = uploadSide;
        this.name = name;
        this.description = description;
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
    }
}
