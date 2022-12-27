package io.coti.basenode.http.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class NetworkLastKnownNodesResponseData implements ISignable, ISignValidatable, IResponseData {

    private static final long serialVersionUID = 3804637865403728215L;
    private HashMap<Hash, NetworkNodeData> networkLastKnownNodeMap;
    private Hash signerHash;
    private SignatureData signature;

    private NetworkLastKnownNodesResponseData() {
    }

    public NetworkLastKnownNodesResponseData(Map<Hash, NetworkNodeData> networkLastKnownNodeMap) {
        this.networkLastKnownNodeMap = (HashMap<Hash, NetworkNodeData>) networkLastKnownNodeMap;
    }

}
