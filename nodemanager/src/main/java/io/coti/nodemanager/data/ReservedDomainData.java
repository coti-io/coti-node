package io.coti.nodemanager.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class ReservedDomainData implements IEntity {

    private static final long serialVersionUID = 6381266172279543285L;
    @NotNull
    private @Valid Hash domainHash;
    @NotNull
    private @Valid Hash userHash;

    public ReservedDomainData(Hash domainHash, Hash userHash) {
        this.domainHash = domainHash;
        this.userHash = userHash;
    }

    @Override
    public Hash getHash() {
        return domainHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.domainHash = hash;

    }
}
