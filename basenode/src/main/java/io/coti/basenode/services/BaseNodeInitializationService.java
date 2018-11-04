package io.coti.basenode.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.http.GetTransactionBatchRequest;
import io.coti.basenode.http.GetTransactionBatchResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private IDatabaseConnector dataBaseConnector;

    @Autowired
    private IPropagationSubscriber propagationSubscriber;

    @Autowired
    protected IIpService ipService;

    protected String nodeIp;

    private final static String NODE_MANAGER_ADD_NODE_ENDPOINT = "/nodes/new_node";

    private final static String NODE_MANAGER_GET_NETWORK_DETAILS_ENDPOINT = "/nodes/all";

    private final static String RECOVERY_NODE_GET_BATCH_ENDPOINT = "/getTransactionBatch";

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
            dataBaseConnector.init();
            propagationSubscriber.startListening();
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
                List<TransactionData> missingTransactions = requestMissingTransactions(transactionIndexService.getLastTransactionIndexData().getIndex() + 1);
                if (missingTransactions != null) {
                    int threadPoolSize = 20;
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
        classNameToSubscriberHandlerMapping.put(Channel.getChannelString(NetworkDetails.class, getNodeProperties().getNodeType()),
                newNetwork -> networkService.handleNetworkChanges((NetworkDetails) newNetwork));

        monitorService.init();
        propagationSubscriber.addMessageHandler(classNameToSubscriberHandlerMapping);
        propagationSubscriber.connectAndSubscribeToServer(networkService.getNetworkDetails().getNodeManagerPropagationAddress());
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
                            networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_BATCH_ENDPOINT,
                            new GetTransactionBatchRequest(firstMissingTransactionIndex),
                            GetTransactionBatchResponse.class);
            log.info("Received transaction batch of size: {}", getTransactionBatchResponse.getTransactions().size());
            return getTransactionBatchResponse.getTransactions();
        } catch (Exception e) {
            log.error("Unresponsive recovery NetworkNodeData: {}", networkService.getRecoveryServerAddress());
            log.error(e.getMessage());
            return null;
        }
    }

    public void connectToNetwork() {
        NetworkNodeData networkNodeData = getNodeProperties();
        ResponseEntity<String> addNewNodeResponse = addNewNodeToNodeManager(networkNodeData);
        if(!addNewNodeResponse.getStatusCode().equals(HttpStatus.OK)){
            log.error("Couldn't add node to node manager. Message from NodeManager: {}", addNewNodeResponse);
            System.exit(-1);
        }
        networkService.saveNetwork(getNetworkDetailsFromNodeManager());
    }

    private ResponseEntity<String> addNewNodeToNodeManager(NetworkNodeData networkNodeData) {
        RestTemplate restTemplate = new RestTemplate();
        String newNodeURL = nodeManagerAddress + NODE_MANAGER_ADD_NODE_ENDPOINT;
        return restTemplate.postForEntity(newNodeURL, networkNodeData, String.class);
    }

    private NetworkDetails getNetworkDetailsFromNodeManager(){
        RestTemplate restTemplate = new RestTemplate();
        String newNodeURL = nodeManagerAddress + NODE_MANAGER_GET_NETWORK_DETAILS_ENDPOINT;
        return restTemplate.getForEntity(newNodeURL, NetworkDetails.class).getBody();
    }



    protected abstract NetworkNodeData getNodeProperties();

}
