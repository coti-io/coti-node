package io.coti.historynode.services;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.messages.VoteMessageData;
import io.coti.basenode.data.messages.StateMessageData;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.interfaces.ICommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Service
public class InitializationService extends BaseNodeInitializationService {

    @Autowired
    private ICommunicationService communicationService;
    @Value("${server.port}")
    private String serverPort;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${server.url}")
    private String webServerUrl;
    private EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);

    @PostConstruct
    @Override
    public void init() {
        try {
            super.init();
            super.initDB();
            super.createNetworkNodeData();
            super.getNetwork();

            publisherNodeTypeToMessageTypesMap.put(NodeType.ZeroSpendServer, Arrays.asList(TransactionData.class, DspConsensusResult.class, StateMessageData.class, VoteMessageData.class));
            publisherNodeTypeToMessageTypesMap.put(NodeType.FinancialServer, Collections.singletonList(TransactionData.class));

            communicationService.initSubscriber(NodeType.HistoryNode, publisherNodeTypeToMessageTypesMap);
            communicationService.initPublisher(propagationPort, NodeType.HistoryNode);

            NetworkNodeData zerospendNetworkNodeData = networkService.getSingleNodeData(NodeType.ZeroSpendServer);
            if (zerospendNetworkNodeData == null) {
                log.error("No zerospend server exists in the network got from the node manager, about to exit application");
                System.exit(SpringApplication.exit(applicationContext));
            }
            networkService.setRecoveryServerAddress(zerospendNetworkNodeData.getHttpFullAddress());
            communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress(), NodeType.ZeroSpendServer);
            networkService.addListToSubscription(networkService.getMapFromFactory(NodeType.DspNode).values());
            if (networkService.getSingleNodeData(NodeType.FinancialServer) != null) {
                networkService.addListToSubscription(new ArrayList<>(Collections.singletonList(networkService.getSingleNodeData(NodeType.FinancialServer))));
            }

            super.initServices();
        } catch (CotiRunTimeException e) {
            log.error("Errors at {}", this.getClass().getSimpleName());
            e.logMessage();
            System.exit(SpringApplication.exit(applicationContext));
        } catch (Exception e) {
            log.error("Errors at {}", this.getClass().getSimpleName());
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            System.exit(SpringApplication.exit(applicationContext));
        }
    }

    @Override
    protected NetworkNodeData createNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.HistoryNode, nodeIp, serverPort, NodeCryptoHelper.getNodeHash(), networkType);
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setWebServerUrl(webServerUrl);
        return networkNodeData;
    }


}
