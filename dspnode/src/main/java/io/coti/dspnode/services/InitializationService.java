package io.coti.dspnode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.NodeProperties;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class InitializationService {
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${node.manager.address}")
    private String nodeManagerAddress;

    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private CommunicationService communicationService;

    @PostConstruct
    public void init() {
        String zerospendServerAddress = getAllNodes();
        List<String> dspNodeAddresses = getDspNodeAddresses();
        RegisterToNodeManager();
        NodeProperties zeroSpendNodeProperties = getNodeProperties(zerospendServerAddress);

        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                transactionService.handleNewTransactionFromFullNode((TransactionData) data));
        classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                addressService.handleNewAddressFromFullNode((AddressData) data));

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSender(Arrays.asList(zeroSpendNodeProperties.getReceivingAddress()));
        communicationService.initSubscriber(propagationServerAddresses, NodeType.DspNode);
        communicationService.initPropagator(propagationPort);

        baseNodeInitializationService.init();
    }

    private NodeProperties getNodeProperties(String zerospendServerAddress) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(zerospendServerAddress + "/nodeProperties", NodeProperties.class);
    }

    private void RegisterToNodeManager() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(nodeManagerAddress + "/nodes/newDsp", "http://localhost:8060", String.class);
    }

    private String getAllNodes() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(nodeManagerAddress + "/nodes/all", String.class);
    }

    private List<String> getDspNodeAddresses() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(nodeManagerAddress + "/nodes/dsps", List.class);
    }
}
