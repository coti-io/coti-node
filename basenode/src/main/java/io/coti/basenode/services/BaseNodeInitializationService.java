package io.coti.basenode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTransactionBatchRequest;
import io.coti.basenode.http.GetTransactionBatchResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.IAddressService;
import io.coti.basenode.services.interfaces.IBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class BaseNodeInitializationService {
    @Value("${recovery.server.address}")
    private String recoveryServerAddress;

    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private LiveViewService liveViewService;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private BaseNodeTransactionService transactionService;
    @Autowired
    private IAddressService addressService;

    public void init() {
        try {
            addressService.init();
            balanceService.init();
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
        } catch (Exception e) {
            log.error("Errors at {} : ", this.getClass().getSimpleName(), e);
            System.exit(-1);
        }
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