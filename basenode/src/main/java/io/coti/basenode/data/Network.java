package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Network implements IEntity {
    public List<Node> dspNodes;
    public List<Node> fullNodes;
    public List<Node> trustScoreNodes;
    Node zerospendServer;

    public Network() {
        dspNodes = new ArrayList<>();
        fullNodes = new ArrayList<>();
        trustScoreNodes = new ArrayList<>();
    }

    public void addNode(Node node) {
        switch (node.nodeType) {
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
        }
    }

    @Override
    public Hash getHash() {
        return this.getHash();
    }

    @Override
    public void setHash(Hash hash) {
    }
}