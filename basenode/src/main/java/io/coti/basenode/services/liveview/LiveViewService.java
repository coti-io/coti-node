package io.coti.basenode.services.liveview;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.liveview.data.GraphData;
import io.coti.basenode.services.liveview.data.GraphTransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Vector;

@Slf4j
@Service
public class LiveViewService {

    @Autowired
    private SimpMessagingTemplate messagingSender;

    private GraphData graphData;

    public LiveViewService() {
        this.graphData = new GraphData();
        this.graphData.transactions = new Vector<>();
    }

    public GraphData getFullGraph() {
        return graphData;
    }

    public void addTransaction(TransactionData transactionData) {
        GraphTransactionData graphTransactionData = new GraphTransactionData();
        graphTransactionData.setId(transactionData.getHash().toHexString());
        graphTransactionData.setTrustScore(transactionData.getSenderTrustScore());
        graphTransactionData.setGenesis(transactionData.isGenesis());

        if (transactionData.isSource()) {
            graphTransactionData.setStatus(0);
        } else {
            graphTransactionData.setStatus(transactionData.isTrustChainConsensus() ? 2 : 1);
        }

        if (transactionData.getLeftParentHash() != null) {
            graphTransactionData.setLeftParent(transactionData.getLeftParentHash().toHexString());
        }
        if (transactionData.getRightParentHash() != null) {
            graphTransactionData.setRightParent(transactionData.getRightParentHash().toHexString());
        }
        setGraphTransactionDataDatesFromTransactionData(transactionData, graphTransactionData);
        graphData.transactions.add(graphTransactionData);

        sendTransaction(graphTransactionData);
    }

    public void updateTransactionStatus(TransactionData transactionData, int newStatus) {
        GraphTransactionData graphTransactionData = new GraphTransactionData();
        graphTransactionData.setId(transactionData.getHash().toHexString());
        graphTransactionData.setTrustScore(transactionData.getSenderTrustScore());
        graphTransactionData.setStatus(newStatus);
        setGraphTransactionDataDatesFromTransactionData(transactionData, graphTransactionData);
        int index = graphData.transactions.indexOf(graphTransactionData);
        if (index == -1) {
            graphData.transactions.add(graphTransactionData);
        } else {
            graphData.transactions.set(index, graphTransactionData);
        }
        sendTransaction(graphTransactionData);
    }

    public void setGraphTransactionDataDatesFromTransactionData(TransactionData transactionData, GraphTransactionData graphTransactionData) {
        graphTransactionData.setAttachmentTime(transactionData.getAttachmentTime());
        graphTransactionData.setTransactionConsensusUpdateTime(transactionData.getTransactionConsensusUpdateTime());
        if (transactionData.getAttachmentTime() != null && transactionData.getTransactionConsensusUpdateTime() != null) {
            graphTransactionData.setTccDuration(Duration.between(transactionData.getAttachmentTime(), transactionData.getTransactionConsensusUpdateTime()).toMillis() / 1000);
        }
    }

    private void sendTransaction(GraphTransactionData graphTransactionData) {
        messagingSender.convertAndSend("/topic/live-transactions", graphTransactionData);
    }
}