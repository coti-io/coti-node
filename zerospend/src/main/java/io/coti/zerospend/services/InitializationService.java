package io.coti.zerospend.services;

import io.coti.common.NodeType;
import io.coti.common.data.DspVote;
import io.coti.common.services.BaseNodeInitializationService;
import io.coti.common.services.CommunicationService;
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
    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;

    @PostConstruct
    public void init() {
        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(DspVote.class.getName(), data ->
                dspVoteService.receiveDspVote((DspVote) data));

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSubscriber(propagationServerAddresses, NodeType.ZeroSpendServer);
        communicationService.initPropagator(propagationPort);
        communicationService.initSender(receivingServerAddresses);

        baseNodeInitializationService.init();
    }
}