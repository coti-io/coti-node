package io.coti.basenode.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.data.Network;
import io.coti.basenode.data.Node;
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

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Slf4j
@Service
public abstract class BaseNodeInitializationService {

    @Value("${node.manager.address}")
    private String nodeManagerAddress;

    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private IBalanceService balanceService;
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
    @Autowired
    protected INetworkService networkService;
    protected Node node;

    @PostConstruct
    public void init() {
        try {
            initCommunication();
            log.info("The communication initialization is done");
            initTransactionSync();
            log.info("The transaction sync initialization is done");
        } catch (Exception e) {
            log.error("Errors at {} : ", this.getClass().getSimpleName(), e);
            System.exit(-1);
        }
    }

    private void initTransactionSync() throws Exception {
        addressService.init();
        balanceService.init();
        dspVoteService.init();
        transactionService.init();
        potService.init();
        AtomicLong maxTransactionIndex = new AtomicLong(-1);
        transactions.forEach(transactionData -> handleExistingTransaction(maxTransactionIndex, transactionData));
        transactionIndexService.init(maxTransactionIndex);

        if (!networkService.getRecoveryServerAddress().isEmpty()) {
            List<TransactionData> missingTransactions = requestMissingTransactions(maxTransactionIndex.get() + 1);
            if (missingTransactions != null) {
                missingTransactions.forEach(transactionData ->
                        transactionService.handlePropagatedTransaction(transactionData));
            }
        }
        balanceService.finalizeInit();
        clusterService.finalizeInit();
        log.info("Transactions Load completed");
    }

    private void initCommunication() {
        HashMap<String, Consumer<Object>> classNameToSubscriberHandlerMapping = new HashMap<>();
        classNameToSubscriberHandlerMapping.put(Channel.getChannelString(Network.class, getNodeProperties().getNodeType()), newNetwork ->
                {
                    networkService.handleNetworkChanges((Network) newNetwork);
                });

        monitorService.init();

        propagationSubscriber.init(classNameToSubscriberHandlerMapping);
        propagationSubscriber.addAddress(networkService.getNetwork().nodeManagerPropagationAddress);
        propagationSubscriber.subscribeToChannels();
        networkService.connectToCurrentNetwork();
    }

    private void initSubscriber(){

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
                            networkService.getRecoveryServerAddress() + "/getTransactionBatch",
                            new GetTransactionBatchRequest(firstMissingTransactionIndex),
                            GetTransactionBatchResponse.class);
            log.info("Received transaction batch of size: {}", getTransactionBatchResponse.getTransactions().size());
            return getTransactionBatchResponse.getTransactions();
        } catch (Exception e) {
            log.error("Unresponsive recovery Node: {}", networkService.getRecoveryServerAddress());
            log.error(e.getMessage());
            return null;
        }
    }

    public void connectToNetwork(){
        node = getNodeProperties();
        Network network = connectToNodeManager(node);
        networkService.saveNetwork(network);
    }

    private Network connectToNodeManager(Node node) {
        RestTemplate restTemplate = new RestTemplate();
        String newNodeURL = nodeManagerAddress + "/nodes/newNode";
        return restTemplate.postForEntity(newNodeURL, node, Network.class).getBody();
    }

    protected abstract Node getNodeProperties();

}
