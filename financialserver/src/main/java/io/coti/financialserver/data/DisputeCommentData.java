package io.coti.financialserver.data;

import lombok.Data;
import java.util.Date;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;

@Data
public class DisputeCommentData implements IEntity {
    private Hash hash;
    private ActionSide commentSide;
    private String comment;
    private Date creationTime;

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {

    }
}
