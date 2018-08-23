package io.coti.common.services;

import io.coti.common.model.Transactions;
import io.coti.common.services.LiveView.LiveViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class InitializationService {
    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private LiveViewService liveViewService;
    @Autowired
    private TransactionHelper transactionHelper;
    @PostConstruct
    public void init() {
        AtomicLong maxTransactionIndex = new AtomicLong(-1);
        transactions.forEach(transactionData -> {
            if (!transactionData.isTrustChainConsensus()) {
                clusterService.addUnconfirmedTransaction(transactionData);
            }
            liveViewService.addNode(transactions.getByHash(transactionData.getHash()));
            balanceService.insertSavedTransaction(transactionData);
            if (transactionData.getDspConsensusResult() != null) {
                maxTransactionIndex.set(Math.max(maxTransactionIndex.get(), transactionData.getDspConsensusResult().getIndex()));
            }
            transactionHelper.incrementTotalTransactions();
        });
        log.info("Transactions Load completed");
        balanceService.finalizeInit();
        clusterService.finalizeInit();
        transactionIndexService.init(maxTransactionIndex);
        monitorService.init();

    }

}