package io.coti.financialserver.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.financialserver.data.ReservedAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import static io.coti.financialserver.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class InitializationService extends BaseNodeInitializationService {

    private static final int COTI_GENESIS_ADDRESS_INDEX = Math.toIntExact(ReservedAddress.GENESIS_ONE.getIndex());
    private final EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${server.port}")
    private String serverPort;
    @Value("${server.url}")
    private String webServerUrl;
    @Value("${financialserver.seed.key:}")
    private String seed;
    @Value("${secret.financialserver.seed.name.key:}")
    private String seedSecretName;
    @Value("${token.generation.fee:1}")
    private BigDecimal defaultTokenGenerationFee;
    @Value("${token.minting.fee:1}")
    private BigDecimal defaultTokenMintingFee;

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
                seed = secretManagerService.getSecret(seed, seedSecretName, "seed");
                log.info("Please generate Native token at ZeroSpend with following genesis address: {}", nodeIdentityService.generateAddress(seed, COTI_GENESIS_ADDRESS_INDEX));
                log.error("No zerospend server exists in the network got from the node manager. Exiting from the application");
                System.exit(SpringApplication.exit(applicationContext));
            }
            networkService.setRecoveryServer(zerospendNetworkNodeData);
            communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress(), NodeType.ZeroSpendServer);
            networkService.addListToSubscription(networkService.getMapFromFactory(NodeType.DspNode).values());
            communicationService.addSender(zerospendNetworkNodeData.getReceivingFullAddress(), NodeType.ZeroSpendServer);

            ConstantTokenFeeData defaultGenerationNodeConstantTokenFeeData = new ConstantTokenFeeData("*", NodeFeeType.TOKEN_GENERATION_FEE, defaultTokenGenerationFee);
            ConstantTokenFeeData defaultMintingNodeConstantTokenFeeData = new ConstantTokenFeeData("*", NodeFeeType.TOKEN_MINTING_FEE, defaultTokenMintingFee);
            defaultTokenFeeDataList.add(defaultGenerationNodeConstantTokenFeeData);
            defaultTokenFeeDataList.add(defaultMintingNodeConstantTokenFeeData);
            super.initServices();

            distributionService.init();
            distributionService.distributeToInitialFunds();
            fundDistributionService.init();
            fundDistributionService.initReservedBalance();
            disputeService.init();
            distributeTokenService.init();
            feeService.init();
            rollingReserveService.init();
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
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.FinancialServer, version, nodeIp, serverPort, nodeIdentityService.getNodeHash(), networkType, monitorService.getLastTotalHealthState());
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setWebServerUrl(webServerUrl);
        return networkNodeData;

    }
}

