package io.coti.dspnode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
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
    @Value("#{'${zerospend.receiving.address}'.split(',')}")
    private List<String> receivingServerAddresses;
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
        String zerospendServerAddress = getZeroSpendAddress();
        List<String> dspNodeAddresses = getDspNodeAddresses();
        RegisterToNodeManager();

        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                transactionService.handleNewTransactionFromFullNode((TransactionData) data));
        classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                addressService.handleNewAddressFromFullNode((AddressData) data));

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSender(receivingServerAddresses);
        communicationService.initSubscriber(propagationServerAddresses, NodeType.DspNode);
        communicationService.initPropagator(propagationPort);

        baseNodeInitializationService.init();
    }

    private void RegisterToNodeManager() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(nodeManagerAddress + "/nodes/newDsp", "localhost:8060", String.class);
    }

    private String getZeroSpendAddress() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(nodeManagerAddress + "/nodes/zerospend", String.class);
    }

    private List<String> getDspNodeAddresses() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(nodeManagerAddress + "/nodes/newDsp", "localhost:8060", String.class);
        return restTemplate.getForObject(nodeManagerAddress + "/nodes/zerospend", List.class);
    }
}
