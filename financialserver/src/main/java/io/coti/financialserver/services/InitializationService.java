package io.coti.financialserver.services;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class InitializationService {
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;
    @Value("${propagation.port}")
    private String propagationPort;

    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;
    @Autowired
    private CommunicationService communicationService;
    @Autowired
    RollingReserveService rollingReserveService;

    @PostConstruct
    public void init() {
        communicationService.initSubscriber(propagationServerAddresses, NodeType.FinancialServer);

        communicationService.initPropagator(propagationPort);
        baseNodeInitializationService.init();
    }
}

