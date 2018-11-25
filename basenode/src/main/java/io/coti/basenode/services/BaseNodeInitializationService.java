package io.coti.basenode.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.database.Interfaces.IRocksDBConnector;
import io.coti.basenode.http.GetTransactionBatchResponse;
import io.coti.basenode.http.data.KYCApprovementResponse;
import io.coti.basenode.http.data.KYCApprovmentRequest;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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

    private final static String NODE_MANAGER_NODES_ENDPOINT = "/nodes";
    private final static String RECOVERY_NODE_GET_BATCH_ENDPOINT = "/transaction_batch";
    private final static String STARTING_INDEX_URL_PARAM_ENDPOINT = "?starting_index=";
    @Autowired
    protected INetworkService networkService;
    @Value("${server.ip}")
    protected String nodeIp;
    private NetworkNodeData nodeData;
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
    private IRocksDBConnector dataBaseConnector;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private KYCApprovementService kycApprovementService;
    @Autowired
    private INetworkDetailsService networkDetailsService;

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

    private void initTransactionSync() {
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
        } catch (Exception e) {
            log.error("Fatal error in initialization", e);
            System.exit(-1);
        }
    }

    private void initCommunication() {
        HashMap<String, Consumer<Object>> classNameToSubscriberHandlerMapping = new HashMap<>();
        classNameToSubscriberHandlerMapping.put(Channel.getChannelString(NetworkDetails.class, this.nodeData.getNodeType()),
                newNetwork -> networkService.handleNetworkChanges((NetworkDetails) newNetwork));

        monitorService.init();
        propagationSubscriber.addMessageHandler(classNameToSubscriberHandlerMapping);
        propagationSubscriber.connectAndSubscribeToServer(networkDetailsService.getNetworkDetails().getNodeManagerPropagationAddress());
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
        try {
            GetTransactionBatchResponse getTransactionBatchResponse =
                    restTemplate.getForObject(
                            networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_BATCH_ENDPOINT
                                    + STARTING_INDEX_URL_PARAM_ENDPOINT + firstMissingTransactionIndex,
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
        this.nodeData = createNodeProperties();
        ResponseEntity<String> addNewNodeResponse = addNewNodeToNodeManager(nodeData);
        if (!addNewNodeResponse.getStatusCode().equals(HttpStatus.OK)) {
            log.error("Couldn't add node to node manager. Message from NodeManager: {}", addNewNodeResponse);
            System.exit(-1);
        }
        networkDetailsService.setNetworkDetails(getNetworkDetailsFromNodeManager());
    }

    private ResponseEntity<String> addNewNodeToNodeManager(NetworkNodeData networkNodeData) {
        try {
            handleCCAApprovement(networkNodeData);
            networkNodeData.setTrustScore(networkNodeData.getKycApprovementResponse().getTrustScore());
            String newNodeURL = nodeManagerAddress + NODE_MANAGER_NODES_ENDPOINT;
            HttpEntity<NetworkNodeData> entity = new HttpEntity<>(networkNodeData);
            return restTemplate.exchange(newNodeURL, HttpMethod.PUT, entity, String.class);
        } catch (Exception ex) {
            log.error("Error connecting node manager, please check node's address / contact COTI");
            System.exit(-1);
        }
        return ResponseEntity.noContent().build();
    }

    private NetworkDetails getNetworkDetailsFromNodeManager() {
        String newNodeURL = nodeManagerAddress + NODE_MANAGER_NODES_ENDPOINT;
        return restTemplate.getForEntity(newNodeURL, NetworkDetails.class).getBody();
    }

    private KYCApprovmentRequest createCCAApprovementRequest(NetworkNodeData networkNodeData){
        return new KYCApprovmentRequest(networkNodeData.getNodeHash(), networkNodeData.getSignature());
    }

    private void handleCCAApprovement(NetworkNodeData networkNodeData){
        KYCApprovmentRequest KYCApprovmentRequest = createCCAApprovementRequest(networkNodeData);
        ResponseEntity<KYCApprovementResponse> ccaApprovementResponseEntity = kycApprovementService.sendCCAApprovment(KYCApprovmentRequest);
        log.info("Response has returned from cca: {}", ccaApprovementResponseEntity);
        KYCApprovementResponse approvementResponse = ccaApprovementResponseEntity.getBody();
        if(approvementResponse != null){
            networkNodeData.setKycApprovementResponse(approvementResponse);
        }
        else{
            log.error("cca returned a null object: {} . closing server", ccaApprovementResponseEntity);
            System.exit(-1);
        }
    }


    protected abstract NetworkNodeData createNodeProperties();

}
