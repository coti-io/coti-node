package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

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

    private Map<NodeType, Map<Hash, NetworkNodeData>> nodeMapsFactory;



    public NetworkDetails() {
        dspNetworkNodesMap = new ConcurrentHashMap<>();
        fullNodeNetworkNodesMap = new ConcurrentHashMap<>();
        trustScoreNetworkNodesMap = new ConcurrentHashMap<>();

        nodeMapsFactory = new EnumMap<>(NodeType.class);
        nodeMapsFactory.put(NodeType.FullNode, fullNodeNetworkNodesMap);
        nodeMapsFactory.put(NodeType.DspNode, dspNetworkNodesMap);
        nodeMapsFactory.put(NodeType.TrustScoreNode, trustScoreNetworkNodesMap);

    }

    public Map<Hash, NetworkNodeData>  getListFromFactory(NodeType nodeType) {
        Map<Hash, NetworkNodeData> mapToGet = nodeMapsFactory.get(nodeType);
        if(mapToGet == null){
            log.error("Unsupported networkNodeData type ( {} ) is not deleted", nodeType);
            return Collections.emptyMap();
        }
        return mapToGet;
    }
    

    @Override
    public Hash getHash() {
        return new Hash(1);
    }

    @Override
    public void setHash(Hash hash) {

    }
}