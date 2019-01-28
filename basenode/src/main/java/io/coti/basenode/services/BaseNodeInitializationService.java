package io.coti.basenode.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.communication.ZeroMQSubscriber;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.crypto.KYCApprovementResponseCrypto;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.data.KYCResponseRecordData;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.http.GetTransactionBatchResponse;
import io.coti.basenode.http.data.KYCApprovementRequest;
import io.coti.basenode.http.data.KYCApprovementResponse;
import io.coti.basenode.model.KYCResponseRecords;
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

    private final static String NODE_MANAGER_NODES_ENDPOINT = "/transactions";
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
    private IDatabaseConnector databaseConnector;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private KYCApprovementService kycApprovementService;
    @Autowired
    private INetworkDetailsService networkDetailsService;
    @Autowired
    private KYCApprovementResponseCrypto kycApprovementResponseCrypto;
    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;
    @Autowired
    private KYCResponseRecords kycResponseRecords;

    public void init() {
        try {
            addressService.init();
            balanceService.init();
            confirmationService.init();
            dspVoteService.init();
            transactionService.init();
            potService.init();
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
            propagationSubscriber.startListening();
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
        HashMap<String, Consumer<Object>> channelToSubscriberHandlerMap = new HashMap<>();
        channelToSubscriberHandlerMap.put(Channel.getChannelString(NetworkData.class, this.nodeData.getNodeType()),
                newNetworkData -> networkService.handleNetworkChanges((NetworkData) newNetworkData));

        monitorService.init();
        propagationSubscriber.addMessageHandler(channelToSubscriberHandlerMap);
        propagationSubscriber.connectAndSubscribeToServer(networkDetailsService.getNetworkData().getNodeManagerPropagationAddress());
        propagationSubscriber.initPropagationHandler();

    }

    public void initDB() {
        databaseConnector.init();
    }

    private void handleExistingTransaction(AtomicLong maxTransactionIndex, TransactionData transactionData) {
        if (!transactionData.isTrustChainConsensus()) {
            clusterService.addUnconfirmedTransaction(transactionData);
        }
        liveViewService.addTransaction(transactionData);
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
            log.error("Error at missing transactions from recovery Node");
            throw new RuntimeException(e);
        }
    }

    public void connectToNetwork() {
        this.nodeData = createNodeProperties();
        ResponseEntity<String> addNewNodeResponse = addNewNodeToNodeManager(nodeData);
        if (!addNewNodeResponse.getStatusCode().equals(HttpStatus.OK)) {
            log.error("Couldn't add node to node manager. Message from NodeManager: {}", addNewNodeResponse);
            System.exit(-1);
        }
        networkDetailsService.setNetworkData(getNetworkDetailsFromNodeManager());
    }

    private ResponseEntity<String> addNewNodeToNodeManager(NetworkNodeData networkNodeData) {
        try {
            KYCResponseRecordData kycResponseRecordData = kycResponseRecords.getByHash(networkNodeData.getHash());
            if (kycResponseRecordData != null && kycResponseRecordData.isValid()) {
                networkNodeData.setKycApprovementResponse(kycResponseRecordData.getKycApprovementResponse());
            } else {
                handleKycServerApprovement(networkNodeData);
            }
            networkNodeCrypto.signMessage(networkNodeData);
            String newNodeURL = nodeManagerAddress + NODE_MANAGER_NODES_ENDPOINT;
            HttpEntity<NetworkNodeData> entity = new HttpEntity<>(networkNodeData);
            return restTemplate.exchange(newNodeURL, HttpMethod.PUT, entity, String.class);
        } catch (Exception ex) {
            log.error("Error connecting node manager, please check node's address / contact COTI ex:", ex);
            System.exit(-1);
        }
        return ResponseEntity.noContent().build();
    }

    private NetworkData getNetworkDetailsFromNodeManager() {
        String newNodeURL = nodeManagerAddress + NODE_MANAGER_NODES_ENDPOINT;
        return restTemplate.getForEntity(newNodeURL, NetworkData.class).getBody();
    }

    private KYCApprovementRequest createKycServerApprovementRequest(NetworkNodeData networkNodeData) {
        return new KYCApprovementRequest(networkNodeData.getNodeHash(), networkNodeData.getNodeSignature(), networkNodeData.getNodeType());
    }

    private void handleKycServerApprovement(NetworkNodeData networkNodeData) {
        KYCApprovementRequest kycApprovementRequest = createKycServerApprovementRequest(networkNodeData);
        ResponseEntity<KYCApprovementResponse> kycServerApprovementResponseEntity = kycApprovementService.sendKycServerApprovement(kycApprovementRequest);
        log.info("Response has returned from KYC server: {}", kycServerApprovementResponseEntity);
        KYCApprovementResponse approvementResponse = kycServerApprovementResponseEntity.getBody();
        if (approvementResponse != null) {

            if (validateCCAApprovementResponse(networkNodeData, approvementResponse)) {
                networkNodeData.setKycApprovementResponse(approvementResponse);
                saveKYCResponseRecord(networkNodeData, approvementResponse, true);
            }
        } else {
            log.error("cca returned a null object: {} . closing server", kycServerApprovementResponseEntity);
            saveKYCResponseRecord(networkNodeData, approvementResponse, false);
            System.exit(-1);
        }
    }

    protected boolean validateCCAApprovementResponse(NetworkNodeData networkNodeData, KYCApprovementResponse approvementResponse) {
        if (!networkNodeData.getNodeType().equals(approvementResponse.getNodeType())) {
            log.error("KYCApprovementResponse has different node type than the request! response: {} wanted nodeType: {}" +
                    " . shutting down server", approvementResponse, networkNodeData.getNodeType());
            saveKYCResponseRecord(networkNodeData, approvementResponse, false);
            System.exit(-1);
            return false;
        }
        /*
        if(!kycApprovementResponseCrypto.verifySignature(approvementResponse)){
            log.error("KYCApprovementResponse failed signature validation! response: {} " +
                    " . node type {}. shutting down server", approvementResponse, networkNodeData.getNodeType());
            saveKYCResponseRecord(networkNodeData, approvementResponse, false);
            System.exit(-1);
            return false;
        }
        */
        return true;
    }

    private void saveKYCResponseRecord(NetworkNodeData networkNodeData, KYCApprovementResponse kycApprovementResponse, boolean valid) {
        KYCResponseRecordData kycResponseRecordData = new KYCResponseRecordData();
        kycResponseRecordData.setHash(networkNodeData.getHash());
        kycResponseRecordData.setKycApprovementResponse(kycApprovementResponse);
        kycResponseRecordData.setValid(valid);
        kycResponseRecords.put(kycResponseRecordData);
    }


    protected abstract NetworkNodeData createNodeProperties();

}
