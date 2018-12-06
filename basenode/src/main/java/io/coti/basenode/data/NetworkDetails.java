package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.security.core.parameters.P;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Slf4j
public class NetworkDetails implements IEntity {
    private Map<Hash, NetworkNodeData> dspNetworkNodesMap;
    private Map<Hash, NetworkNodeData> fullNodeNetworkNodesMap;
    private Map<Hash, NetworkNodeData> trustScoreNetworkNodesMap;
    private String nodeManagerPropagationAddress;
    private NetworkNodeData zerospendServer;




    public NetworkDetails() {
        dspNetworkNodesMap = new ConcurrentHashMap<>();
        fullNodeNetworkNodesMap = new ConcurrentHashMap<>();
        trustScoreNetworkNodesMap = new ConcurrentHashMap<>();
    }

    public Map<Hash, NetworkNodeData> getMapByEnum(NodeType nodeType) {
        switch (nodeType){
            case DspNode: {
                return dspNetworkNodesMap;
            }
            case TrustScoreNode:{
                return trustScoreNetworkNodesMap;
            }
            case FullNode:{
                return fullNodeNetworkNodesMap;
            }
            default:{
                log.error("Unsupported networkNodeData type ( {} ) is not deleted", nodeType);
                return Collections.emptyMap();
            }
        }
    }
    

    @Override
    public Hash getHash() {
        return new Hash(1);
    }

    @Override
    public void setHash(Hash hash) {

    }
}