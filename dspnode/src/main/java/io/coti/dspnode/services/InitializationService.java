package io.coti.dspnode.services;

import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.INetworkDetailsService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class InitializationService extends BaseNodeInitializationService {
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${node.manager.address}")
    private String nodeManagerAddress;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private INetworkDetailsService networkDetailsService;

    @PostConstruct
    public void init() {
        super.initDB();
        super.connectToNetwork();
        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                transactionService.handleNewTransactionFromFullNode((TransactionData) data));

        classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                addressService.handleNewAddressFromFullNode((AddressData) data));

        communicationService.initSubscriber(NodeType.DspNode);
        NetworkNodeData zerospendNetworkNodeData = networkDetailsService.getNetworkDetails().getZerospendServer();
        if (zerospendNetworkNodeData == null) {
            log.error("No zerospend server exists in the network got from the node manager, about to exit application");
            System.exit(-1);
        }
        networkService.setRecoveryServerAddress(zerospendNetworkNodeData.getHttpFullAddress());
        communicationService.initPropagator(propagationPort);
        super.init();

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.addSender(zerospendNetworkNodeData.getReceivingFullAddress());
        communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress());
        List<NetworkNodeData> dspNetworkNodeDataList = networkDetailsService
                .getShuffledNetworkNodeDataListFromMapValues(networkDetailsService.getNetworkDetails().getDspNetworkNodesMap());
        dspNetworkNodeDataList.removeIf(dsp -> dsp.getAddress().equals(nodeIp) && dsp.getHttpPort().equals(serverPort));
        dspNetworkNodeDataList.forEach(node -> communicationService.addSubscription(node.getPropagationFullAddress()));

    }

    @Override
    protected NetworkNodeData createNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.DspNode, nodeIp, serverPort, NodeCryptoHelper.getNodeHash());
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setReceivingPort(receivingPort);
        return networkNodeData;
    }
}
