package io.coti.fullnode.services;

import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.INetworkDetailsService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class InitializationService extends BaseNodeInitializationService {
    @Autowired
    private CommunicationService communicationService;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;
    @Autowired
    private INetworkDetailsService networkDetailsService;

    @Value("${fee.percentage}")
    private Double nodeFee;

    @PostConstruct
    public void init() {
        super.connectToNetwork();
        communicationService.initSubscriber(NodeType.FullNode);

        List<NetworkNodeData> dspNetworkNodeData = networkDetailsService.getNetworkDetails().getDspNetworkNodesList();
        if(!dspNetworkNodeData.isEmpty()){
            networkService.setRecoveryServerAddress(dspNetworkNodeData.get(0).getHttpFullAddress());
        }
        super.init();

        Collections.shuffle(dspNetworkNodeData);
        for (int i = 0; i < dspNetworkNodeData.size() && i < 2; i++) {
            communicationService.addSubscription(dspNetworkNodeData.get(i).getPropagationFullAddress());
            communicationService.addSender(dspNetworkNodeData.get(i).getReceivingFullAddress());
        }

    }

    protected NetworkNodeData createNodeProperties() {
        if(validateFeePercentage(nodeFee)){
            NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.FullNode, nodeIp, serverPort, NodeCryptoHelper.getNodeHash(), nodeFee);
            networkNodeCrypto.signMessage(networkNodeData);
            return networkNodeData;
        }
        return new NetworkNodeData();
    }

    private boolean validateFeePercentage(Double feePercentage){
        if (feePercentage < 0.0 || feePercentage > 100.0) {
            log.error("Fee Percentage is invalid, please fix fee.percentage property by following coti instructions. " +
                    "Shutting down the server! feePercentage: {} ", feePercentage);
            System.exit(-1);
            return false;
        }
        return true;
    }

}