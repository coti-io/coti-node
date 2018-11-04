package io.coti.fullnode.services;

import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.INetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
public class InitializationService extends BaseNodeInitializationService {
    @Autowired
    private CommunicationService communicationService;
    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private INetworkService networkService;
    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;

    @PostConstruct
    public void init() {
        nodeIp = ipService.getIp();
        super.connectToNetwork();
        communicationService.initSubscriber(NodeType.FullNode);
        List<NetworkNodeData> dspNetworkNodeData = this.networkService.getNetworkDetails().getDspNetworkNodesList();
        Collections.shuffle(dspNetworkNodeData);
        if (!dspNetworkNodeData.isEmpty()) {
            NetworkNodeData firstDspNetworkNodeData = dspNetworkNodeData.get(0);
            networkService.setRecoveryServerAddress(firstDspNetworkNodeData.getHttpFullAddress());
            communicationService.addSender(firstDspNetworkNodeData.getReceivingFullAddress());
            communicationService.addSubscription(firstDspNetworkNodeData.getPropagationFullAddress());
            networkService.getNetworkDetails().addNode(firstDspNetworkNodeData);
        }
        if (dspNetworkNodeData.size() > 1) {
            NetworkNodeData secondDspNetworkNodeData = dspNetworkNodeData.get(1);
            communicationService.addSender(secondDspNetworkNodeData.getReceivingFullAddress());
            communicationService.addSubscription(secondDspNetworkNodeData.getPropagationFullAddress());
            networkService.getNetworkDetails().addNode(secondDspNetworkNodeData);

        }
        super.init();
    }

    protected NetworkNodeData getNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.FullNode, nodeIp, serverPort, NodeCryptoHelper.getNodeHash());
        networkNodeCrypto.signMessage(networkNodeData);
        return networkNodeData;
    }
}