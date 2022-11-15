package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.zerospend.http.SetIndexesRequest;
import io.coti.zerospend.http.SetIndexesResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class TransactionService extends BaseNodeTransactionService {

    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IPropagationPublisher propagationPublisher;

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        if (EnumSet.of(TransactionType.Initial).contains(transactionData.getType())) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            dspVoteService.publishDecision(transactionData.getHash());
            return;
        }
        dspVoteService.preparePropagatedTransactionForVoting(transactionData);
    }

    public ResponseEntity<IResponse> setIndexToTransactions(SetIndexesRequest setIndexesRequest) {
        Set<Hash> transactionHashes = setIndexesRequest.getTransactionHashes();
        if (transactionHashes.isEmpty()) {
            transactionHashes = transactionHelper.getNoneIndexedTransactionHashes();
        }
        int requestedIndexNumber = transactionHashes.size();
        AtomicInteger indexedTransactionNumber = new AtomicInteger(0);
        transactionHashes.forEach(transactionHash -> {
            try {
                dspVoteService.publishDecision(transactionHash);
                indexedTransactionNumber.incrementAndGet();
            } catch (Exception e) {
                log.error("Error at indexing for hash {} . Error: {}", transactionHash, e.getMessage());
            }
        });

        return ResponseEntity.ok().body(new SetIndexesResponse(requestedIndexNumber, indexedTransactionNumber.get()));
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 5000)
    public void totalTransactionsAmountFromRecovery() {
        TransactionsStateData transactionsStateData = new TransactionsStateData(transactionHelper.getTotalTransactions());
        propagationPublisher.propagate(transactionsStateData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode));
    }
}