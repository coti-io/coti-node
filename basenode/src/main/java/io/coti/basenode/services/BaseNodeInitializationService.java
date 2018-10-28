package io.coti.basenode.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNode;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTransactionBatchRequest;
import io.coti.basenode.http.GetTransactionBatchResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Slf4j
@Service
public abstract class BaseNodeInitializationService {

    @Autowired
    protected INetworkService networkService;
    protected NetworkNode networkNode;
    @Value("${node.manager.address}")
    private String nodeManagerAddress;
    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IConfirmationService confirmationService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private IMonitorService monitorService;
    @Autowired
    private LiveViewService liveViewService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private ITransactionService transactionService;
    @Autowired
    private IAddressService addressService;
    @Autowired
    private IDspVoteService dspVoteService;
    @Autowired
    private IPotService potService;

    @Autowired
    private IPropagationSubscriber propagationSubscriber;

    public void init() {
        try {
            initTransactionSync();
            log.info("The transaction sync initialization is done");
            initCommunication();
            log.info("The communication initialization is done");
        } catch (Exception e) {
            log.error("Errors at {} : ", this.getClass().getSimpleName(), e);
            System.exit(-1);
        }
    }

    private void initTransactionSync()  {
        try {
            propagationSubscriber.startListeneing();
            addressService.init();
            balanceService.init();
            confirmationService.init();
            dspVoteService.init();
            transactionService.init();
            potService.init();
            AtomicLong maxTransactionIndex = new AtomicLong(-1);
            transactions.forEach(transactionData -> handleExistingTransaction(maxTransactionIndex, transactionData));
            transactionIndexService.init(maxTransactionIndex);

            if (networkService.getRecoveryServerAddress() != null) {
                List<TransactionData> missingTransactions = requestMissingTransactions(maxTransactionIndex.get() + 1);
                if (missingTransactions != null) {
                    int threadPoolSize = 1;
                    log.info("{} threads running for missing transactions", threadPoolSize);
                    ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
                    List<Callable<Object>> missingTransactionsTasks = new ArrayList<>(missingTransactions.size());
                    missingTransactions.forEach(transactionData ->
                            missingTransactionsTasks.add(Executors.callable(() -> transactionService.handlePropagatedTransaction(transactionData))));
                    executorService.invokeAll(missingTransactionsTasks);
                }
            }
            balanceService.validateBalances();
            clusterService.finalizeInit();
            log.info("Transactions Load completed");
        }
        catch (Exception e){
            log.error("Fatal error in initialization", e);
            System.exit(-1);
        }
    }

    private void initCommunication() {
        HashMap<String, Consumer<Object>> classNameToSubscriberHandlerMapping = new HashMap<>();
        classNameToSubscriberHandlerMapping.put(Channel.getChannelString(NetworkData.class, getNodeProperties().getNodeType()),
                newNetwork -> networkService.handleNetworkChanges((NetworkData) newNetwork));

        monitorService.init();
      //  networkService.connectToCurrentNetwork();
        propagationSubscriber.addMessageHandler(classNameToSubscriberHandlerMapping);
        propagationSubscriber.connectAndSubscribeToServer(networkService.getNetworkData().getNodeManagerPropagationAddress());

        propagationSubscriber.initPropagationHandler();

    }

    private void initSubscriber() {

    }

    private void handleExistingTransaction(AtomicLong maxTransactionIndex, TransactionData transactionData) {
        if (!transactionData.isTrustChainConsensus()) {
            clusterService.addUnconfirmedTransaction(transactionData);
        }
        liveViewService.addNode(transactionData);
        confirmationService.insertSavedTransaction(transactionData);
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
                            networkService.getRecoveryServerAddress() + "/getTransactionBatch",
                            new GetTransactionBatchRequest(firstMissingTransactionIndex),
                            GetTransactionBatchResponse.class);
            log.info("Received transaction batch of size: {}", getTransactionBatchResponse.getTransactions().size());
            return getTransactionBatchResponse.getTransactions();
        } catch (Exception e) {
            log.error("Unresponsive recovery NetworkNode: {}", networkService.getRecoveryServerAddress());
            log.error(e.getMessage());
            return null;
        }
    }

    public void connectToNetwork() {
        networkNode = getNodeProperties();
        NetworkData networkData = connectToNodeManager(networkNode);
        networkService.saveNetwork(networkData);
    }

    private NetworkData connectToNodeManager(NetworkNode networkNode) {
        RestTemplate restTemplate = new RestTemplate();
        String newNodeURL = nodeManagerAddress + "/nodes/newNode";
        return restTemplate.postForEntity(newNodeURL, networkNode, NetworkData.class).getBody();
    }

    protected abstract NetworkNode getNodeProperties();

}
