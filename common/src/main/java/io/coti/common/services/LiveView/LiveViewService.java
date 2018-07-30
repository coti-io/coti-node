package io.coti.common.services.LiveView;

import io.coti.common.data.GraphData;
import io.coti.common.data.NodeData;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;
@Slf4j
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
        nodeData.setGenesis(transactionData.isZeroSpend());
        
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
        // AttachmentTime less then 15 minutes
      //  if(((new Date().getTime()  - transactionData.getAttachmentTime().getTime())/ (60 * 1000)) < 15) {

            webSocketSender.sendNode(nodeData);

       // }

    }

    public void updateNodeStatus(TransactionData transactionData, int newStatus) {
        NodeData nodeData = new NodeData();
        nodeData.setId(transactionData.getHash().toHexString());
        nodeData.setTrustScore(transactionData.getSenderTrustScore());
        int currentIndex = graphData.nodes.indexOf(nodeData);
        NodeData newNode = graphData.nodes.get(currentIndex);
        newNode.setStatus(newStatus);
        setNodeDataDatesFromTransactionData(transactionData, newNode);
       // if(((new Date().getTime()  - transactionData.getAttachmentTime().getTime())/ (60 * 1000)) < 15) {

        webSocketSender.sendNode(newNode);
       // }
    }

    public void setNodeDataDatesFromTransactionData(TransactionData transactionData, NodeData nodeData) {
        nodeData.setAttachmentTime(transactionData.getAttachmentTime());
        nodeData.setVerificationTime(transactionData.getVerificationTime());
        nodeData.setTransactionConsensusUpdateTime(transactionData.getTransactionConsensusUpdateTime());
        if (transactionData.getAttachmentTime()!= null && transactionData.getTransactionConsensusUpdateTime()!= null) {
            nodeData.setTccDuration( (transactionData.getTransactionConsensusUpdateTime().getTime() - transactionData.getAttachmentTime().getTime())/ 1000);
        }
    }
}