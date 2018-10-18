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
    Node zerospendServer;
    public String nodeManagerPropagationAddress;

    public Network() {
        dspNodes = Collections.synchronizedList(new ArrayList<>());
        fullNodes = Collections.synchronizedList(new ArrayList<>());
        trustScoreNodes = new ArrayList<>();
    }

    public void addNode(Node node) {
        switch (node.getNodeType()) {
            case DspNode:
                dspNodes.add(node);
                break;
            case FullNode:
                fullNodes.add(node);
                break;
            case TrustScoreNode:
                trustScoreNodes.add(node);
                break;
            case ZeroSpendServer:
                zerospendServer = node;
                break;
            default:
                log.error("Unsupported node type ( {} ) is not added", node.getNodeType());
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