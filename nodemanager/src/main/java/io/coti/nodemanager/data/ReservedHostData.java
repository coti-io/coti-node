package io.coti.nodemanager.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class ReservedHostData implements IEntity {

    private static final long serialVersionUID = 6381266172279543285L;
    @NotNull
    private @Valid Hash hostHash;
    @NotNull
    private @Valid Hash nodeHash;

    public ReservedHostData(Hash hostHash, Hash nodeHash) {
        this.hostHash = hostHash;
        this.nodeHash = nodeHash;
    }

    @Override
    public Hash getHash() {
        return hostHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hostHash = hash;

    }
}
