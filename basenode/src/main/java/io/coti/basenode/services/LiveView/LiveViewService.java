package io.coti.basenode.services.LiveView;

import io.coti.basenode.data.GraphData;
import io.coti.basenode.data.NodeData;
import io.coti.basenode.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Vector;

@Slf4j
@Service
public class LiveViewService {

    @Autowired
    private SimpMessagingTemplate messagingSender;

    private GraphData graphData;

    public LiveViewService() {
        this.graphData = new GraphData();
        this.graphData.nodes = new Vector<>();
    }

    public GraphData getFullGraph() {
        return graphData;
    }

    public void addNode(TransactionData transactionData) {
        NodeData nodeData = new NodeData();
        nodeData.setId(transactionData.getHash().toHexString());
        nodeData.setTrustScore(transactionData.getSenderTrustScore());
        nodeData.setGenesis(transactionData.isGenesis());

        if (transactionData.isSource()) {
            nodeData.setStatus(0);
        } else {
            nodeData.setStatus(transactionData.isTrustChainConsensus() ? 2 : 1);
        }

        if (transactionData.getLeftParentHash() != null) {
            nodeData.setLeftParent(transactionData.getLeftParentHash().toHexString());
        }
        if (transactionData.getRightParentHash() != null) {
            nodeData.setRightParent(transactionData.getRightParentHash().toHexString());
        }
        setNodeDataDatesFromTransactionData(transactionData, nodeData);
        graphData.nodes.add(nodeData);

        sendNode(nodeData);
    }

    public void updateNodeStatus(TransactionData transactionData, int newStatus) {
        NodeData nodeData = new NodeData();
        nodeData.setId(transactionData.getHash().toHexString());
        nodeData.setTrustScore(transactionData.getSenderTrustScore());
        nodeData.setStatus(newStatus);
        setNodeDataDatesFromTransactionData(transactionData, nodeData);
        int index = graphData.nodes.indexOf(nodeData);
        if (index == -1) {
            graphData.nodes.add(nodeData);
        } else {
            graphData.nodes.set(index, nodeData);
        }
        sendNode(nodeData);
    }

    public void setNodeDataDatesFromTransactionData(TransactionData transactionData, NodeData nodeData) {
        nodeData.setAttachmentTime(transactionData.getAttachmentTime());
        nodeData.setTransactionConsensusUpdateTime(transactionData.getTransactionConsensusUpdateTime());
        if (transactionData.getAttachmentTime() != null && transactionData.getTransactionConsensusUpdateTime() != null) {
            nodeData.setTccDuration((transactionData.getTransactionConsensusUpdateTime().getTime() - transactionData.getAttachmentTime().getTime()) / 1000);
        }
    }

    private void sendNode(NodeData nodeData) {
        messagingSender.convertAndSend("/topic/nodes", nodeData);
    }
}