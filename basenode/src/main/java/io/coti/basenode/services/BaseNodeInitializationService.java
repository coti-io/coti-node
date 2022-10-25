package io.coti.basenode.services;

import com.google.gson.Gson;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.crypto.GetNodeRegistrationRequestCrypto;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.NetworkException;
import io.coti.basenode.exceptions.NetworkNodeValidationException;
import io.coti.basenode.exceptions.NodeRegistrationValidationException;
import io.coti.basenode.exceptions.TransactionSyncException;
import io.coti.basenode.http.*;
import io.coti.basenode.model.NodeRegistrations;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public abstract class BaseNodeInitializationService {

    private static final String NODE_REGISTRATION = "/node/node_registration";
    private static final String NODE_MANAGER_NODES_ENDPOINT = "/nodes";
    private static final String LAST_KNOWN_WALLET_ENDPOINT = "/nodes/last";
    private static final String NETWORK_DETAILS_ERROR = "Error at getting network details";
    @Value("${network}")
    protected NetworkType networkType;
    @Value("${server.ip}")
    protected String nodeIp;
    @Value("${node.manager.ip}")
    private String nodeManagerIp;
    @Value("${node.manager.port}")
    private String nodeManagerPort;
    @Value("${node.manager.propagation.port}")
    private String nodeManagerPropagationPort;
    private String nodeManagerHttpAddress;
    @Value("${kycserver.url}")
    private String kycServerAddress;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;
    @Autowired
    protected INetworkService networkService;
    @Autowired
    private IAwsService awsService;
    @Autowired
    private IDatabaseConnector databaseConnector;
    @Autowired
    private IDBRecoveryService dbRecoveryService;
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
    @Autowired
    private IShutDownService shutDownService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private GetNodeRegistrationRequestCrypto getNodeRegistrationRequestCrypto;
    @Autowired
    private NodeRegistrationCrypto nodeRegistrationCrypto;
    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;
    @Autowired
    private NodeRegistrations nodeRegistrations;
    @Autowired
    private BaseNodeClusterStampService clusterStampService;
    @Autowired
    private ITransactionSynchronizationService transactionSynchronizationService;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    private BuildProperties buildProperties;
    protected String version;
    @Autowired
    private ITransactionPropagationCheckService transactionPropagationCheckService;
    @Autowired
    private ICurrencyService currencyService;
    @Autowired
    private IMintingService mintingService;
    private final Map<Long, ReducedExistingTransactionData> indexToTransactionMap = new HashMap<>();
    private EnumMap<InitializationTransactionHandlerType, ExecutorData> existingTransactionExecutorMap;
    @Autowired
    protected IMetricsService metricsService;
    @Autowired
    protected IEventService eventService;
    protected List<NodeFeeType> nodeFeeTypeList = new ArrayList<>();
    @Autowired
    private INodeFeesService nodeFeesService;

    public void init() {
        log.info("Application name: {}, version: {}", buildProperties.getName(), buildProperties.getVersion());
        version = buildProperties.getVersion();
    }

    public void initServices() {
        awsService.init();
        dbRecoveryService.init();
        addressService.init();
        currencyService.init();
        balanceService.init();
        mintingService.init();
        clusterStampService.init();
        confirmationService.init();
        transactionIndexService.init();
        dspVoteService.init();
        transactionService.init();
        transactionPropagationCheckService.init();
        potService.init();
        initCommunication();
        eventService.init();
        log.info("The communication initialization is done");
        initTransactionSync();
        log.info("The transaction sync initialization is done");
        networkService.setConnectToNetworkUrl(nodeManagerHttpAddress + NODE_MANAGER_NODES_ENDPOINT);
        networkService.connectToNetwork();
        propagationSubscriber.initPropagationHandler();
        monitorService.init();
        metricsService.init();
        nodeFeesService.init(nodeFeeTypeList);
    }

    public void initTransactionSync() {
        try {
            log.info("Starting to read existing transactions");
            AtomicLong completedExistedTransactionNumber = new AtomicLong(0);
            Thread monitorExistingTransactions = transactionService.monitorTransactionThread("existing", completedExistedTransactionNumber, null, "Db Txs Monitor");
            final AtomicBoolean executorServicesInitiated = new AtomicBoolean(false);
            final AtomicReference<ExecutorService> handleExistingExecutorService = new AtomicReference<>();
            transactions.forEach(transactionData -> {
                if (!executorServicesInitiated.get()) {
                    existingTransactionExecutorMap = new EnumMap<>(InitializationTransactionHandlerType.class);
                    EnumSet.allOf(InitializationTransactionHandlerType.class).forEach(initializationTransactionHandlerType -> existingTransactionExecutorMap.put(initializationTransactionHandlerType, new ExecutorData(initializationTransactionHandlerType)));
                    handleExistingExecutorService.set(Executors.newFixedThreadPool(10));
                    executorServicesInitiated.set(true);
                }

                if (!monitorExistingTransactions.isAlive()) {
                    monitorExistingTransactions.start();
                }
                handleExistingExecutorService.get().execute(() -> handleExistingTransaction(transactionData));
                completedExistedTransactionNumber.incrementAndGet();
            });
            if (monitorExistingTransactions.isAlive()) {
                monitorExistingTransactions.interrupt();
                monitorExistingTransactions.join();
            }
            if (executorServicesInitiated.get()) {
                existingTransactionExecutorMap.forEach((initializationTransactionHandlerType, executorData) -> executorData.waitForTermination());
                handleExistingExecutorService.get().shutdown();
            }
            confirmationService.setLastDspConfirmationIndex(indexToTransactionMap);
            indexToTransactionMap.clear();
            log.info("Finished to read existing transactions");

            if (networkService.getRecoveryServer() != null) {
                transactionSynchronizationService.requestMissingTransactions(transactionIndexService.getLastTransactionIndexData().getIndex() + 1);
            }
            clusterService.startToCheckTrustChainConfirmation();
            waitingInitialConfirmation();
            balanceService.validateBalances();
            log.info("Transactions Load completed");

        } catch (TransactionSyncException e) {
            throw new TransactionSyncException("Error at sync transactions.\n" + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TransactionSyncException("Error at sync transactions.", e);
        } catch (Exception e) {
            throw new TransactionSyncException("Error at sync transactions.", e);
        }
    }

    private void waitingInitialConfirmation() throws InterruptedException {
        Object initialTccConfirmationLock = confirmationService.getInitialTccConfirmationLock();
        synchronized (initialTccConfirmationLock) {
            while (!confirmationService.getInitialTccConfirmationFinished().get()) {
                initialTccConfirmationLock.wait(100);
            }
        }
    }

    private void initCommunication() {
        networkService.setNodeManagerPropagationAddress("tcp://" + nodeManagerIp + ":" + nodeManagerPropagationPort);

        propagationSubscriber.connectAndSubscribeToServer(networkService.getNodeManagerPropagationAddress(), NodeType.NodeManager);
        propagationSubscriber.startListening();
    }

    protected void initDB() {
        databaseConnector.init();
    }

    private void handleExistingTransaction(TransactionData transactionData) {
        existingTransactionExecutorMap.get(InitializationTransactionHandlerType.CLUSTER).submit(() -> clusterService.addExistingTransactionOnInit(transactionData));
        existingTransactionExecutorMap.get(InitializationTransactionHandlerType.CONFIRMATION).submit(() -> {
            confirmationService.insertSavedTransaction(transactionData, indexToTransactionMap);
            currencyService.handleExistingTransaction(transactionData);
            mintingService.handleExistingTransaction(transactionData);
        });
        existingTransactionExecutorMap.get(InitializationTransactionHandlerType.TRANSACTION).submit(() -> transactionService.addDataToMemory(transactionData));
        eventService.handleExistingTransaction(transactionData);
        transactionHelper.incrementTotalTransactions();
    }

    protected void createNetworkNodeData() {
        networkService.init();
        NetworkNodeData networkNodeData = createNodeProperties();
        NodeRegistrationData nodeRegistrationData = nodeRegistrations.getByHash(networkNodeData.getHash());
        if (nodeRegistrationData != null) {
            networkNodeData.setNodeRegistrationData(nodeRegistrationData);
        } else {
            getNodeRegistration(networkNodeData);
        }
        networkNodeCrypto.signMessage(networkNodeData);
        networkService.setNetworkNodeData(networkNodeData);
    }

    protected void getNetwork() {

        nodeManagerHttpAddress = "http://" + nodeManagerIp + ":" + nodeManagerPort;
        networkService.setNetworkLastKnownNodeMap(getNetworkLastKnownNodesFromNodeManager());
        networkService.setNetworkData(getNetworkDetailsFromNodeManager());
    }

    private HashMap<Hash, NetworkNodeData> getNetworkLastKnownNodesFromNodeManager() {
        GetNetworkLastKnownNodesResponse networkLastKnownNodesResponse;
        try {
            networkLastKnownNodesResponse = restTemplate.getForObject(nodeManagerHttpAddress + LAST_KNOWN_WALLET_ENDPOINT, GetNetworkLastKnownNodesResponse.class);
        } catch (HttpStatusCodeException e) {
            throw new NetworkException("Error at getting network details. Node manager error: " + new Gson().fromJson(e.getResponseBodyAsString(), Response.class));
        } catch (Exception e) {
            throw new NetworkException(NETWORK_DETAILS_ERROR, e);
        }
        if (networkLastKnownNodesResponse == null) {
            throw new NetworkException("Null network from node manager");
        }

        try {
            networkService.verifyNodeManager(networkLastKnownNodesResponse.getNetworkLastKnownNodesResponseData());
        } catch (NetworkNodeValidationException e) {
            throw new NetworkException(NETWORK_DETAILS_ERROR, e);
        }

        return networkLastKnownNodesResponse.getNetworkLastKnownNodesResponseData().getNetworkLastKnownNodeMap();
    }

    private NetworkData getNetworkDetailsFromNodeManager() {
        NetworkData networkData;
        try {
            networkData = restTemplate.getForObject(nodeManagerHttpAddress + NODE_MANAGER_NODES_ENDPOINT, NetworkData.class);
        } catch (HttpStatusCodeException e) {
            throw new NetworkException("Error at getting network details. Node manager error: " + new Gson().fromJson(e.getResponseBodyAsString(), Response.class));
        } catch (Exception e) {
            throw new NetworkException(NETWORK_DETAILS_ERROR, e);
        }
        if (networkData == null) {
            throw new NetworkException("Null network from node manager");
        }

        try {
            networkService.verifyNodeManager(networkData);
        } catch (NetworkNodeValidationException e) {
            throw new NetworkException(NETWORK_DETAILS_ERROR, e);
        }
        return networkData;
    }

    private void getNodeRegistration(NetworkNodeData networkNodeData) {
        try {
            restTemplate.setRequestFactory(new CustomHttpComponentsClientHttpRequestFactory());
            GetNodeRegistrationRequest getNodeRegistrationRequest = new GetNodeRegistrationRequest(networkNodeData.getNodeType(), networkType);
            getNodeRegistrationRequestCrypto.signMessage(getNodeRegistrationRequest);

            GetNodeRegistrationResponse getNodeRegistrationResponse =
                    restTemplate.postForObject(
                            kycServerAddress + NODE_REGISTRATION,
                            getNodeRegistrationRequest,
                            GetNodeRegistrationResponse.class);
            log.info("Node registration received");
            if (getNodeRegistrationResponse == null) {
                throw new NodeRegistrationValidationException("Null node registration response.");
            }
            NodeRegistrationData nodeRegistrationData = getNodeRegistrationResponse.getNodeRegistrationData();
            validateNodeRegistrationResponse(nodeRegistrationData, networkNodeData);

            networkNodeData.setNodeRegistrationData(nodeRegistrationData);
            nodeRegistrations.put(nodeRegistrationData);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new NetworkException(String.format("Error at registration of node. Registrar response: %n %s", e.getResponseBodyAsString()), e);
        } catch (Exception e) {
            throw new NetworkException("Error at registration of node.", e);
        }
    }

    protected void validateNodeRegistrationResponse(NodeRegistrationData nodeRegistrationData, NetworkNodeData networkNodeData) {
        if (nodeRegistrationData == null) {
            throw new NodeRegistrationValidationException("Null node registration data.");
        }
        if (!nodeRegistrationData.getSignerHash().toString().equals(kycServerPublicKey)) {
            throw new NodeRegistrationValidationException("Invalid kyc server public key.");
        }
        if (!networkNodeData.getNodeHash().equals(nodeRegistrationData.getNodeHash()) || !networkNodeData.getNodeType().equals(nodeRegistrationData.getNodeType())) {
            throw new NodeRegistrationValidationException("Node registration response has invalid fields.");
        }
        if (!nodeRegistrationCrypto.verifySignature(nodeRegistrationData)) {
            throw new NodeRegistrationValidationException("Node registration failed signature validation! Shutting down server");
        }
    }

    protected abstract NetworkNodeData createNodeProperties();

    @PreDestroy
    public void shutdown() {
        Thread.currentThread().setName("PreDestroy");
        shutDownService.shutdown();
    }

}
