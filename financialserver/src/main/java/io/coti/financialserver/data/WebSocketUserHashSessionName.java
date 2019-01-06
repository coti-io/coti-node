package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class WebSocketUserHashSessionName implements IEntity {

    @NotNull
    private Hash userHash;

    @NotNull
    private String webSocketUserName;

    public WebSocketUserHashSessionName(Hash userHash) {
        this.userHash = userHash;
    }

    @Override
    public Hash getHash() {
        return userHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.userHash = hash;
    }

}
