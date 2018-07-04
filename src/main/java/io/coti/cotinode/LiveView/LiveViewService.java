package io.coti.cotinode.LiveView;

import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.websocket.WebSocketSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class LiveViewService {

    @Autowired
    private WebSocketSender webSocketSender;

    private GraphData graphData;

    public LiveViewService() {
        this.graphData = new GraphData();
        this.graphData.nodes = new LinkedList<>();
    }

    public GraphData getFullGraph() {
        return graphData;
    }

    public void addNode(TransactionData transactionData) {
        NodeData nodeData = new NodeData();
        nodeData.setId(transactionData.getHash().toHexString());
        nodeData.setTrustScore(transactionData.getSenderTrustScore());

        if (transactionData.getLeftParentHash() != null) {
            nodeData.setLeftParent(transactionData.getLeftParentHash().toHexString());
        }
        if (transactionData.getRightParentHash() != null) {
            nodeData.setRightParent(transactionData.getRightParentHash().toHexString());
        }
        graphData.nodes.add(nodeData);
        webSocketSender.sendNode(nodeData);
    }

    public void updateNodeStatus(TransactionData transactionData, int newStatus) {
        NodeData nodeData = new NodeData();
        nodeData.setId(transactionData.getHash().toHexString());
        nodeData.setTrustScore(transactionData.getSenderTrustScore());
        int currentIndex = graphData.nodes.indexOf(nodeData);
        NodeData newNode = graphData.nodes.get(currentIndex);
        newNode.setStatus(newStatus);
        webSocketSender.sendNode(newNode);
    }
}