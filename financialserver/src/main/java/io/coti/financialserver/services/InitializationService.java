package io.coti.financialserver.services;

import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.interfaces.ICommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;

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
    private ICommunicationService communicationService;
    private EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);

    @PostConstruct
    public void init() {
        communicationService.initSubscriber(NodeType.FinancialServer, publisherNodeTypeToMessageTypesMap);

        communicationService.initPublisher(propagationPort, NodeType.FinancialServer);
        baseNodeInitializationService.init();
    }
}

