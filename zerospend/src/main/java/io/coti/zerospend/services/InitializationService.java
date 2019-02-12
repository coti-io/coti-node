package io.coti.zerospend.services;

import io.coti.basenode.data.DspReadyForClusterStampData;
import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Service
public class InitializationService {
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;
    @Value("${propagation.port}")
    private String propagationPort;

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private ClusterStampService clusterStampService;
    @Autowired
    private Transactions transactions;

    @PostConstruct
    public void init() {
        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(DspVote.class.getName(), data ->
                dspVoteService.receiveDspVote((DspVote) data));
        classNameToReceiverHandlerMapping.put(DspReadyForClusterStampData.class.getName(), data ->
                clusterStampService.handleDspNodeReadyForClusterStampMessage((DspReadyForClusterStampData) data));
        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        initSubscriber();
        communicationService.initPropagator(propagationPort);

        baseNodeInitializationService.init();

        if (transactions.isEmpty()) {
            transactionCreationService.createGenesisTransactions();
        }
    }

    public void initSubscriber(){
        HashMap<String, Consumer<Object>> classNameToSubscriberHandler = new HashMap<>();
        communicationService.initSubscriber(propagationServerAddresses, NodeType.ZeroSpendServer, classNameToSubscriberHandler);
    }
}