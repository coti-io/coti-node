package io.coti.common.services;

import io.coti.common.data.TransactionData;
import io.coti.common.model.Transactions;
import io.coti.common.services.LiveView.LiveViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.PriorityBlockingQueue;

@Slf4j
@Service
public class InitializationService {
    @Autowired
    private Transactions transactions;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private LiveViewService liveViewService;

    @PostConstruct
    public void init() {
        transactions.forEach(transactionData -> {
            log.info("Loading transaction: {}", transactionData.getHash());
            if (isUnconfirmed(transactionData)) {
                clusterService.addUnconfirmedTransaction(transactionData);
                liveViewService.addNode(transactions.getByHash(transactionData.getHash()));
            }

            balanceService.insertSavedTransaction(transactionData);
        });

        balanceService.finalizeInit();
        clusterService.finalizeInit();
    }

    private boolean isUnconfirmed(TransactionData transactionData) {
        return !transactionData.isTrustChainConsensus() ||
                (transactionData.getDspConsensusResult() != null && !transactionData.getDspConsensusResult().isDspConsensus());
    }
}