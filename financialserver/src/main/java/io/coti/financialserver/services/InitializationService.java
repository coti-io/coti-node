package io.coti.financialserver.services;

import javax.annotation.PostConstruct;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;

@Service
public class InitializationService {
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;

    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;
    @Autowired
    private CommunicationService communicationService;
    @Autowired
    RollingReserveService rollingReserveService;

    @PostConstruct
    public void init() {
        communicationService.initSubscriber(propagationServerAddresses, NodeType.FinancialServer);

        baseNodeInitializationService.init();
    }
}

