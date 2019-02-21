package io.coti.financialserver.services;

import io.coti.basenode.data.NodeType;
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
    @Autowired
    RollingReserveService rollingReserveService;
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;
    @Value("${propagation.port}")
    private String propagationPort;
    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;
    @Autowired
    private CommunicationService communicationService;

    @PostConstruct
    public void init() {
        initSubscriber();
        communicationService.initPropagator(propagationPort);
        baseNodeInitializationService.init();
    }

    public void initSubscriber(){
        HashMap<String, Consumer<Object>> classNameToSubscriberHandler = new HashMap<>();
        communicationService.initSubscriber(propagationServerAddresses, NodeType.FinancialServer, classNameToSubscriberHandler);
    }
}

