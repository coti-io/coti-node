package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
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

    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private IPropagationSubscriber subscriber;

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
        List<NetworkNodeData> dspNetworkNodeData = this.networkService.getNetworkDetails().getDspNetworkNodesList();
        Collections.shuffle(dspNetworkNodeData);
        NetworkNodeData zerospendNetworkNodeData = this.networkService.getNetworkDetails().getZerospendServer();

        if(zerospendNetworkNodeData != null ) {
            networkService.setRecoveryServerAddress(zerospendNetworkNodeData.getHttpFullAddress());
        }
        else{
            log.error("No zerospend server exists in the network got from the node manager");
            networkService.setRecoveryServerAddress("");
            System.exit(-1);
        }

        communicationService.addSender(zerospendNetworkNodeData.getReceivingFullAddress());
        subscriber.connectAndSubscribeToServer(zerospendNetworkNodeData.getPropagationFullAddress());

        dspNetworkNodeData.removeIf(dsp -> dsp.getAddress().equals(serverIp) && dsp.getHttpPort().equals(serverPort) );
        if(dspNetworkNodeData.size() > 0){
                dspNetworkNodeData.forEach(dspnode -> subscriber.connectAndSubscribeToServer(dspnode.getPropagationFullAddress()));
        }
        super.init();
    }

    @Override
    protected NetworkNodeData getNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.DspNode, serverIp, serverPort, NodeCryptoHelper.getNodeHash());
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setReceivingPort(receivingPort);
        networkNodeCrypto.signMessage(networkNodeData);

        return networkNodeData;
    }
}
