package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;

@Data
public class DisputeDocumentData implements IEntity {
    private Hash hash;
    private ActionSide uploadSide;
    private String description;
    private String documentName;
    private Date creationTime;

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
    }
}
