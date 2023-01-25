package io.coti.zerospend.services;

import io.coti.basenode.data.*;
import io.coti.basenode.http.SetIndexesRequest;
import io.coti.basenode.http.SetIndexesResponse;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.zerospend.services.NodeServiceManager.*;

@Service
@Slf4j
@Primary
public class TransactionService extends BaseNodeTransactionService {

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

    @Override
    public ResponseEntity<IResponse> setIndexToTransactions(SetIndexesRequest setIndexesRequest) {
        Set<Hash> transactionHashes = setIndexesRequest.getTransactionHashes();
        if (transactionHashes.isEmpty()) {
            transactionHashes = nodeTransactionHelper.getNoneIndexedTransactionHashes();
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
        TransactionsStateData transactionsStateData = new TransactionsStateData(nodeTransactionHelper.getTotalTransactions());
        propagationPublisher.propagate(transactionsStateData, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode));
    }
}