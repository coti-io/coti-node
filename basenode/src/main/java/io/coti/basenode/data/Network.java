package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
public class Network implements IEntity {
    public List<Node> dspNodes;
    public List<Node> fullNodes;
    public List<Node> trustScoreNodes;
    public String nodeManagerPropagationAddress;
    Node zerospendServer;

    public Network() {
        dspNodes = Collections.synchronizedList(new ArrayList<>());
        fullNodes = Collections.synchronizedList(new ArrayList<>());
        trustScoreNodes = new ArrayList<>();
    }

    public void addNode(Node node) {
        switch (node.getNodeType()) {
            case DspNode:
                if (!dspNodes.contains(node)) {
                    dspNodes.add(node);
                }
                break;
            case FullNode:
                if (!fullNodes.contains(node)) {
                    fullNodes.add(node);
                }
                break;
            case TrustScoreNode:
                if (!trustScoreNodes.contains(node)) {
                    trustScoreNodes.add(node);
                }
                break;
            case ZeroSpendServer:
                    zerospendServer = node;
                break;
            default:
                log.error("Unsupported node type ( {} ) is not added", node.getNodeType());
                return;
        }
    }

    public void removeNode(Node node){
        switch (node.getNodeType()) {
            case DspNode:
                if (dspNodes.contains(node)) {
                dspNodes.remove(node);
            }
            break;
            case FullNode:
                if (fullNodes.contains(node)) {
                    fullNodes.remove(node);
                }
                break;
            case TrustScoreNode:
                if (trustScoreNodes.contains(node)) {
                    trustScoreNodes.remove(node);
                }
                break;
            case ZeroSpendServer:
                zerospendServer = null;
                break;
            default:
                log.error("Unsupported node type ( {} ) is not deleted", node.getNodeType());
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