package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.soap.Node;
import java.util.*;

@Data
@Slf4j
public class NetworkDetails implements IEntity {
    private List<NetworkNodeData> dspNetworkNodesList;
    private List<NetworkNodeData> fullNetworkNodesList;
    private List<NetworkNodeData> trustScoreNetworkNodesList;
    private String nodeManagerPropagationAddress;
    private NetworkNodeData zerospendServer;

    private Map<NodeType, List<NetworkNodeData>> nodeMapsFactory;



    public NetworkDetails() {
        dspNetworkNodesList = Collections.synchronizedList(new ArrayList<>());
        fullNetworkNodesList = Collections.synchronizedList(new ArrayList<>());
        trustScoreNetworkNodesList = Collections.synchronizedList(new ArrayList<>());

        nodeMapsFactory = new EnumMap<>(NodeType.class);
        nodeMapsFactory.put(NodeType.FullNode, fullNetworkNodesList);
        nodeMapsFactory.put(NodeType.DspNode, dspNetworkNodesList);
        nodeMapsFactory.put(NodeType.TrustScoreNode, trustScoreNetworkNodesList);

    }

    public List<NetworkNodeData> getListFromFactory(NodeType nodeType) {
        List<NetworkNodeData> listToGet = nodeMapsFactory.get(nodeType);
        if(listToGet == null){
            log.error("Unsupported networkNodeData type ( {} ) is not deleted", nodeType);
            return Collections.emptyList();
        }
        return listToGet;
    }
    

    @Override
    public Hash getHash() {
        return new Hash(1);
    }

    @Override
    public void setHash(Hash hash) {

    }
}