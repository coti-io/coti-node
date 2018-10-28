package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
public class NetworkData implements IEntity {
    private List<NetworkNode> dspNetworkNodes;
    private List<NetworkNode> fullNetworkNodes;
    private List<NetworkNode> trustScoreNetworkNodes;
    private String nodeManagerPropagationAddress;
    private NetworkNode zerospendServer;

    public NetworkData() {
        dspNetworkNodes = Collections.synchronizedList(new ArrayList<>());
        fullNetworkNodes = Collections.synchronizedList(new ArrayList<>());
        trustScoreNetworkNodes = new ArrayList<>();
    }

    public void addNode(NetworkNode networkNode) {
        switch (networkNode.getNodeType()) {
            case DspNode:
                if (!dspNetworkNodes.contains(networkNode)) {
                    dspNetworkNodes.add(networkNode);
                }
                break;
            case FullNode:
                if (!fullNetworkNodes.contains(networkNode)) {
                    fullNetworkNodes.add(networkNode);
                }
                break;
            case TrustScoreNode:
                if (!trustScoreNetworkNodes.contains(networkNode)) {
                    trustScoreNetworkNodes.add(networkNode);
                }
                break;
            case ZeroSpendServer:
                zerospendServer = networkNode;
                break;
            default:
                log.error("Unsupported networkNode type ( {} ) is not added", networkNode.getNodeType());
                return;
        }
    }

    public void removeNode(NetworkNode networkNode) {
        switch (networkNode.getNodeType()) {
            case DspNode:
                if (dspNetworkNodes.contains(networkNode)) {
                    dspNetworkNodes.remove(networkNode);
                }
                break;
            case FullNode:
                if (fullNetworkNodes.contains(networkNode)) {
                    fullNetworkNodes.remove(networkNode);
                }
                break;
            case TrustScoreNode:
                if (trustScoreNetworkNodes.contains(networkNode)) {
                    trustScoreNetworkNodes.remove(networkNode);
                }
                break;
            case ZeroSpendServer:
                zerospendServer.setAddress("");
                break;
            default:
                log.error("Unsupported networkNode type ( {} ) is not deleted", networkNode.getNodeType());
                return;
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