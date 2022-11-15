package io.coti.financialserver.services;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.interfaces.ICommunicationService;
import io.coti.financialserver.data.ReservedAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

@Slf4j
@Service
public class InitializationService extends BaseNodeInitializationService {

    private static final int COTI_GENESIS_ADDRESS_INDEX = Math.toIntExact(ReservedAddress.GENESIS_ONE.getIndex());
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${server.port}")
    private String serverPort;
    @Value("${server.url}")
    private String webServerUrl;
    @Value("${financialserver.seed}")
    private String seed;
    @Autowired
    private ICommunicationService communicationService;
    private final EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);
    @Autowired
    private DistributionService distributionService;
    @Autowired
    private FundDistributionService fundDistributionService;

    @PostConstruct
    @Override
    public void init() {
        try {
            super.init();
            super.initDB();
            super.createNetworkNodeData();
            super.getNetwork();

            publisherNodeTypeToMessageTypesMap.put(NodeType.ZeroSpendServer, Arrays.asList(TransactionData.class, DspConsensusResult.class, TransactionsStateData.class));
            communicationService.initSubscriber(NodeType.FinancialServer, publisherNodeTypeToMessageTypesMap);

            communicationService.initPublisher(propagationPort, NodeType.FinancialServer);

            NetworkNodeData zerospendNetworkNodeData = networkService.getSingleNodeData(NodeType.ZeroSpendServer);
            if (zerospendNetworkNodeData == null) {
                log.info("Please generate Native token at ZeroSpend with following genesis address: {}", NodeCryptoHelper.generateAddress(seed, COTI_GENESIS_ADDRESS_INDEX));
                log.error("No zerospend server exists in the network got from the node manager. Exiting from the application");
                System.exit(SpringApplication.exit(applicationContext));
            }
            networkService.setRecoveryServerAddress(zerospendNetworkNodeData.getHttpFullAddress());
            communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress(), NodeType.ZeroSpendServer);
            networkService.addListToSubscription(networkService.getMapFromFactory(NodeType.DspNode).values());

            nodeFeeTypeList.addAll(Arrays.asList(NodeFeeType.TOKEN_MINTING_FEE, NodeFeeType.TOKEN_GENERATION_FEE));
            super.initServices();

            distributionService.distributeToInitialFunds();
            fundDistributionService.initReservedBalance();
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
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.FinancialServer, version, nodeIp, serverPort, NodeCryptoHelper.getNodeHash(), networkType);
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setWebServerUrl(webServerUrl);
        return networkNodeData;

    }
}

