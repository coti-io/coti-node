package io.coti.basenode.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class NetworkLastKnownNodesResponseData implements IPropagatable, ISignable, ISignValidatable {

    private static final long serialVersionUID = 3804637865403728215L;
    private HashMap<Hash, NetworkNodeData> networkLastKnownNodes;
    private Hash signerHash;
    private SignatureData signature;

    public NetworkLastKnownNodesResponseData() {
    }

    public NetworkLastKnownNodesResponseData(Map<Hash, NetworkNodeData> networkLastKnownNodes) {
        this.networkLastKnownNodes = (HashMap<Hash, NetworkNodeData>) networkLastKnownNodes;
    }

    @Override
    public Hash getHash() {
        return new Hash(this.hashCode());
    }

    @Override
    public void setHash(Hash hash) {
        // no implementation
    }

}
