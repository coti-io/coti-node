package io.coti.common.services;

import io.coti.common.data.TransactionData;
import io.coti.common.http.GetTransactionBatchRequest;
import io.coti.common.http.GetTransactionBatchResponse;
import io.coti.common.model.TransactionIndexes;
import io.coti.common.model.Transactions;
import io.coti.common.services.LiveView.LiveViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class InitializationService {
    @Value("${recovery.server.address}")
    private String recoveryServerAddress;
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
    @Autowired
    private TransactionService transactionService;

    @PostConstruct
    public void init() {
        AtomicLong maxTransactionIndex = new AtomicLong(-1);
        transactions.forEach(transactionData -> handleExistingTransaction(maxTransactionIndex, transactionData));
        transactionIndexService.init(maxTransactionIndex);
        log.info("Transactions Load completed");
        monitorService.init();

        if (recoveryServerAddress != null) {
            List<TransactionData> missingTransactions = requestMissingTransactions(maxTransactionIndex.get() + 1);
            if (missingTransactions != null) {
                missingTransactions.forEach(transactionData ->
                        transactionService.handlePropagatedTransaction(transactionData));
            }
        }

        balanceService.finalizeInit();
        clusterService.finalizeInit();
    }

    private void handleExistingTransaction(AtomicLong maxTransactionIndex, TransactionData transactionData) {
        if (!transactionData.isTrustChainConsensus()) {
            clusterService.addUnconfirmedTransaction(transactionData);
        }
        liveViewService.addNode(transactionData);
        balanceService.insertSavedTransaction(transactionData);
        if (transactionData.getDspConsensusResult() != null) {
            maxTransactionIndex.set(Math.max(maxTransactionIndex.get(), transactionData.getDspConsensusResult().getIndex()));
        } else {
            transactionHelper.addNoneIndexedTransaction(transactionData);
        }
        transactionHelper.incrementTotalTransactions();
    }

    private List<TransactionData> requestMissingTransactions(long firstMissingTransactionIndex) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            GetTransactionBatchResponse getTransactionBatchResponse =
                    restTemplate.postForObject(
                            recoveryServerAddress + "/getTransactionBatch",
                            new GetTransactionBatchRequest(firstMissingTransactionIndex),
                            GetTransactionBatchResponse.class);
            log.info("Received transaction batch of size: {}", getTransactionBatchResponse.getTransactions().size());
            return getTransactionBatchResponse.getTransactions();
        } catch (Exception e) {
            log.error("Unresponsive recovery Node: {}", recoveryServerAddress);
            log.error(e.getMessage());
            return null;
        }

    }

}