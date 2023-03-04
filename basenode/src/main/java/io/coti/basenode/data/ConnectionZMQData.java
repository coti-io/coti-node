package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

@Data
public class ConnectionZMQData implements IPropagatable {

    private static final long serialVersionUID = 5578761426811343628L;
    private String connectionAddress;
    private NodeType connectionNodeType;
    private boolean info;

    private ConnectionZMQData() {
    }

    public ConnectionZMQData(String connectionAddress, NodeType connectionNodeType, boolean info) {
        this.connectionAddress = connectionAddress;
        this.connectionNodeType = connectionNodeType;
        this.info = info;
    }

    public ConnectionZMQData(String connectionAddress, NodeType connectionNodeType) {
        this.connectionAddress = connectionAddress;
        this.connectionNodeType = connectionNodeType;
        this.info = false;
    }

    @Override
    @JsonIgnore
    public Hash getHash() {
        int hash = 7;
        if (connectionAddress == null) {
            return new Hash(hash);
        }
        for (int i = 0; i < connectionAddress.length(); i++) {
            hash = hash * 31 + connectionAddress.charAt(i);
        }
        return new Hash(hash);
    }

    @Override
    public void setHash(Hash hash) {
        // no implementation
    }
}
