package io.coti.fullnode.services;

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
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import static io.coti.fullnode.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class InitializationService extends BaseNodeInitializationService {

    private final EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);

    @Value("${server.port}")
    private String serverPort;
    @Value("${minimumFee}")
    private BigDecimal minimumFee;
    @Value("${maximumFee}")
    private BigDecimal maximumFee;
    @Value("${fee.percentage}")
    private BigDecimal nodeFee;
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

            publisherNodeTypeToMessageTypesMap.put(NodeType.DspNode, Arrays.asList(TransactionData.class, RejectedTransactionData.class, AddressData.class, DspConsensusResult.class, TransactionsStateData.class));

            communicationService.initSubscriber(NodeType.FullNode, publisherNodeTypeToMessageTypesMap);

            List<NetworkNodeData> dspNetworkNodeData = networkService.getShuffledNetworkNodeDataListFromMapValues(NodeType.DspNode);
            if (!dspNetworkNodeData.isEmpty()) {
                networkService.setRecoveryServer(dspNetworkNodeData.get(0));
            }
            for (int i = 0; i < dspNetworkNodeData.size() && i < 2; i++) {
                communicationService.addSubscription(dspNetworkNodeData.get(i).getPropagationFullAddress(), NodeType.DspNode);
                communicationService.addSender(dspNetworkNodeData.get(i).getReceivingFullAddress(), NodeType.DspNode);
                ((NetworkService) networkService).addToConnectedDspNodes(dspNetworkNodeData.get(i));
            }

            super.initServices();
            feeService.init();
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

    protected NetworkNodeData createNodeProperties() {
        FeeData feeData = new FeeData(nodeFee, minimumFee, maximumFee);
        if (networkService.validateFeeData(feeData)) {
            NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.FullNode, version, nodeIp, serverPort, nodeIdentityService.getNodeHash(), networkType, monitorService.getLastTotalHealthState());
            networkNodeData.setFeeData(feeData);
            networkNodeData.setWebServerUrl(webServerUrl);
            return networkNodeData;
        }
        log.error("Fee Data is invalid, please fix fee properties by following coti instructions. Shutting down the server!");
        System.exit(SpringApplication.exit(applicationContext));
        return null;
    }
}
