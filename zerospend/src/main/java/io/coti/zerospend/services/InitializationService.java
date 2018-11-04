package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.INetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Service
public class InitializationService extends BaseNodeInitializationService {
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;
    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private IPropagationSubscriber subscriber;

    @PostConstruct
    public void init() {
        nodeIp = ipService.getIp();
        super.connectToNetwork();
        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(
                DspVote.class.getName(), data ->
                        dspVoteService.receiveDspVote((DspVote) data));
        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSubscriber(NodeType.ZeroSpendServer);
        communicationService.initPropagator(propagationPort);
        List<NetworkNodeData> dspList = networkService.getNetworkDetails().getDspNetworkNodesList();
        if (!dspList.isEmpty()) {
            dspList.forEach(dspNode -> {
                subscriber.connectAndSubscribeToServer(dspNode.getPropagationFullAddress());
                networkService.getNetworkDetails().addNode(dspNode);
            });
        }
        super.init();
        if (transactions.isEmpty()) {
            transactionCreationService.createGenesisTransactions();
        }
    }

    protected NetworkNodeData getNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.ZeroSpendServer, nodeIp, serverPort, NodeCryptoHelper.getNodeHash());
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setReceivingPort(receivingPort);
        networkNodeCrypto.signMessage(networkNodeData);
        return networkNodeData;
    }
}