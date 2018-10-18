package io.coti.dspnode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
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
public class InitializationService extends BaseNodeInitializationService{
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${node.manager.address}")
    private String nodeManagerAddress;
    @Value("${server.ip}")
    private String serverIp;
    @Value("${server.port}")
    private String serverPort;
    @Value("${server.ip}")
    private String nodeIp;

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private INetworkService networkService;

    @PostConstruct
    public void init() {
        super.connectToNetwork();
        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                transactionService.handleNewTransactionFromFullNode((TransactionData) data));

        classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                addressService.handleNewAddressFromFullNode((AddressData) data));

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSubscriber(NodeType.DspNode);
        communicationService.initPropagator(propagationPort);
        List<Node> dspNodes = this.networkService.getNetwork().dspNodes;
        Collections.shuffle(dspNodes);
        Node zerospendNode = this.networkService.getNetwork().getZerospendServer();

        if(zerospendNode != null ) {
            networkService.setRecoveryServerAddress(zerospendNode.getHttpFullAddress());
        }
        else{
            networkService.setRecoveryServerAddress("");
        }
        super.init();
        if(zerospendNode != null ){
            communicationService.addSender(zerospendNode.getAddress(), zerospendNode.getReceivingPort());
            communicationService.addSubscription(zerospendNode.getAddress(), zerospendNode.getPropagationPort());
        }
        dspNodes.removeIf(dsp -> dsp.getAddress().equals(serverIp) && dsp.getHttpPort().equals(serverPort) );
        if(dspNodes.size() > 0){
                dspNodes.forEach(dspnode -> communicationService.addSubscription(dspnode.getAddress(), dspnode.getPropagationPort()));
        }

    }

    @Override
    protected Node getNodeProperties() {
        Node node = new Node(NodeType.DspNode, nodeIp, serverPort);
        node.setPropagationPort(propagationPort);
        node.setReceivingPort(receivingPort);
        return node;
    }
}
