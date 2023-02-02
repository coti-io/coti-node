package io.coti.trustscore.services;

import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.BaseNodeInitializationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

import static io.coti.basenode.services.BaseNodeServiceManager.*;
import static io.coti.trustscore.services.NodeServiceManager.trustScoreService;

@Slf4j
@Service
@Primary
public class InitializationService extends BaseNodeInitializationService {

    private final EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);
    @Value("${server.port}")
    private String serverPort;
    @Value("${server.url}")
    private String webServerUrl;

    @PostConstruct
    @Override
    public void init() {
        try {
            super.init();
            super.initDB();
            super.createNetworkNodeData();
            super.getNetwork();

            publisherNodeTypeToMessageTypesMap.put(NodeType.ZeroSpendServer, Arrays.asList(TransactionData.class, DspConsensusResult.class, TransactionsStateData.class));
            publisherNodeTypeToMessageTypesMap.put(NodeType.FinancialServer, Collections.singletonList(TransactionData.class));

            communicationService.initSubscriber(NodeType.TrustScoreNode, publisherNodeTypeToMessageTypesMap);

            NetworkNodeData zerospendNetworkNodeData = networkService.getSingleNodeData(NodeType.ZeroSpendServer);
            if (zerospendNetworkNodeData == null) {
                log.error("No zerospend server exists in the network got from the node manager. Exiting from the application");
                System.exit(SpringApplication.exit(applicationContext));
            }
            networkService.setRecoveryServer(zerospendNetworkNodeData);
            communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress(), NodeType.ZeroSpendServer);
            communicationService.addSender(zerospendNetworkNodeData.getReceivingFullAddress(), NodeType.ZeroSpendServer);
            networkService.addListToSubscription(networkService.getMapFromFactory(NodeType.DspNode).values());
            if (networkService.getSingleNodeData(NodeType.FinancialServer) != null) {
                networkService.addListToSubscription(new ArrayList<>(Collections.singletonList(networkService.getSingleNodeData(NodeType.FinancialServer))));
            }

            super.initServices();
            trustScoreService.init();
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
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.TrustScoreNode, version, nodeIp, serverPort, nodeIdentityService.getNodeHash(), networkType, monitorService.getLastTotalHealthState());
        networkNodeData.setWebServerUrl(webServerUrl);
        return networkNodeData;

    }
}
