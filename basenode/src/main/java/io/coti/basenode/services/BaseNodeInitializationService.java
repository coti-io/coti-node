package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.crypto.GetNodeRegistrationRequestCrypto;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.TransactionSyncException;
import io.coti.basenode.http.CustomHttpComponentsClientHttpRequestFactory;
import io.coti.basenode.http.GetNodeRegistrationRequest;
import io.coti.basenode.http.GetNodeRegistrationResponse;
import io.coti.basenode.model.NodeRegistrations;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.*;
import io.coti.basenode.services.liveview.LiveViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public abstract class BaseNodeInitializationService {

    private final static String NODE_REGISTRATION = "/node/node_registration";
    private final static String NODE_MANAGER_NODES_ENDPOINT = "/nodes";

    @Autowired
    protected INetworkService networkService;
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
    private IClusterStampService clusterStampService;
    @Autowired
    private ITransactionSynchronizationService transactionSynchronizationService;
    @Autowired
    protected ApplicationContext applicationContext;

    public void init() {
        try {
            addressService.init();
            balanceService.init();
            clusterStampService.loadClusterStamp();
            confirmationService.init();
            transactionIndexService.init();
            dspVoteService.init();
            transactionService.init();
            potService.init();
            initCommunication();
            log.info("The communication initialization is done");
            initTransactionSync();
            log.info("The transaction sync initialization is done");
            networkService.setConnectToNetworkUrl(nodeManagerHttpAddress + NODE_MANAGER_NODES_ENDPOINT);
            networkService.connectToNetwork();
            propagationSubscriber.initPropagationHandler();

            monitorService.init();
        } catch (Exception e) {
            log.error("Errors at {}", this.getClass().getSimpleName());
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            System.exit(SpringApplication.exit(applicationContext));
        }
    }

    private void initTransactionSync() {
        try {
            AtomicLong maxTransactionIndex = new AtomicLong(-1);
            log.info("Starting to read existing transactions");
            AtomicLong completedExistedTransactionNumber = new AtomicLong(0);
            Thread monitorExistingTransactions = transactionService.monitorTransactionThread("existing", completedExistedTransactionNumber, null);
            transactions.forEach(transactionData -> {
                if (!monitorExistingTransactions.isAlive()) {
                    monitorExistingTransactions.start();
                }
                handleExistingTransaction(maxTransactionIndex, transactionData);
                completedExistedTransactionNumber.incrementAndGet();
            });
            if (monitorExistingTransactions.isAlive()) {
                monitorExistingTransactions.interrupt();
                monitorExistingTransactions.join();
            }
            confirmationService.setLastDspConfirmationIndex(maxTransactionIndex);
            log.info("Finished to read existing transactions");

            if (networkService.getRecoveryServerAddress() != null) {
                transactionSynchronizationService.requestMissingTransactions(transactionIndexService.getLastTransactionIndexData().getIndex() + 1);
            }
            balanceService.validateBalances();
            log.info("Transactions Load completed");
            clusterService.finalizeInit();

        } catch (Exception e) {
            throw new TransactionSyncException(e.getMessage());
        }
    }

    private void initCommunication() {
        networkService.setNodeManagerPropagationAddress("tcp://" + nodeManagerIp + ":" + nodeManagerPropagationPort);

        propagationSubscriber.connectAndSubscribeToServer(networkService.getNodeManagerPropagationAddress(), NodeType.NodeManager);
        propagationSubscriber.startListening();
    }

    public void initDB() {
        databaseConnector.init();
    }

    private void handleExistingTransaction(AtomicLong maxTransactionIndex, TransactionData transactionData) {
        clusterService.addExistingTransactionOnInit(transactionData);

        liveViewService.addTransaction(transactionData);
        confirmationService.insertSavedTransaction(transactionData, maxTransactionIndex);

        transactionService.addToExplorerIndexes(transactionData);
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
        networkService.setNetworkData(getNetworkDetailsFromNodeManager());
    }

    private NetworkData getNetworkDetailsFromNodeManager() {
        return restTemplate.getForEntity(nodeManagerHttpAddress + NODE_MANAGER_NODES_ENDPOINT, NetworkData.class).getBody();
    }

    private void getNodeRegistration(NetworkNodeData networkNodeData) {
        try {
            restTemplate.setRequestFactory(new CustomHttpComponentsClientHttpRequestFactory());
            GetNodeRegistrationRequest getNodeRegistrationRequest = new GetNodeRegistrationRequest(networkNodeData.getNodeType(), networkType);
            getNodeRegistrationRequestCrypto.signMessage(getNodeRegistrationRequest);

            ResponseEntity<GetNodeRegistrationResponse> getNodeRegistrationResponseEntity =
                    restTemplate.postForEntity(
                            kycServerAddress + NODE_REGISTRATION,
                            getNodeRegistrationRequest,
                            GetNodeRegistrationResponse.class);
            log.info("Node registration received");

            NodeRegistrationData nodeRegistrationData = getNodeRegistrationResponseEntity.getBody().getNodeRegistrationData();
            if (nodeRegistrationData != null && validateNodeRegistrationResponse(nodeRegistrationData, networkNodeData)) {
                networkNodeData.setNodeRegistrationData(nodeRegistrationData);
                nodeRegistrations.put(nodeRegistrationData);
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Error at registration of node. Registrar response: \n {}", e.getResponseBodyAsString());
            System.exit(SpringApplication.exit(applicationContext));
        }
    }

    protected boolean validateNodeRegistrationResponse(NodeRegistrationData nodeRegistrationData, NetworkNodeData networkNodeData) {
        if (!nodeRegistrationData.getSignerHash().toString().equals(kycServerPublicKey)) {
            log.error("Invalid kyc server public key.");
            System.exit(SpringApplication.exit(applicationContext));
        }
        if (!networkNodeData.getNodeHash().equals(nodeRegistrationData.getNodeHash()) || !networkNodeData.getNodeType().equals(nodeRegistrationData.getNodeType())) {
            log.error("Node registration response has invalid fields! Shutting down server.");
            System.exit(SpringApplication.exit(applicationContext));

        }

        if (!nodeRegistrationCrypto.verifySignature(nodeRegistrationData)) {
            log.error("Node registration failed signature validation! Shutting down server");
            System.exit(SpringApplication.exit(applicationContext));
        }

        return true;
    }

    protected abstract NetworkNodeData createNodeProperties();

    @PreDestroy
    public void shutdown() {
        shutDownService.shutdown();
    }

}
