package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class NodeRegistrationData implements IEntity, ISignValidatable {

    @NotNull
    private Hash nodeHash;
    @NotNull
    private NodeType nodeType;
    @NotNull
    private Instant creationTime;
    @NotNull
    private Hash registrationHash;
    @NotNull
    private Hash registrarHash;
    @NotNull
    @Valid
    private SignatureData signature;

    @Override
    public Hash getHash() {
        return nodeHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.nodeHash = hash;
    }

    @Override
    public Hash getSignerHash() {
        return registrarHash;
    }

    @Override
    public SignatureData getSignature() {
        return signature;
    }
    
}
