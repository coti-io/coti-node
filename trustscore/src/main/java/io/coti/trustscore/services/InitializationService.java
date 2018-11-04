package io.coti.trustscore.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
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
    @Autowired
    private INetworkService networkService;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private IPropagationSubscriber subscriber;
    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;

    @PostConstruct
    public void init() {
        nodeIp = ipService.getIp();
        super.connectToNetwork();
        communicationService.initSubscriber(NodeType.TrustScoreNode);
        List<NetworkNodeData> dspNetworkNodeData = this.networkService.getNetworkDetails().getDspNetworkNodesList();
        NetworkNodeData zerospendNetworkNodeData = this.networkService.getNetworkDetails().getZerospendServer();
        if (zerospendNetworkNodeData != null) {
            networkService.setRecoveryServerAddress(zerospendNetworkNodeData.getHttpFullAddress());
            subscriber.connectAndSubscribeToServer(zerospendNetworkNodeData.getPropagationFullAddress());
        }
        if (!dspNetworkNodeData.isEmpty()) {
            Collections.shuffle(dspNetworkNodeData);
            dspNetworkNodeData.forEach(dspnode -> subscriber.connectAndSubscribeToServer(dspnode.getPropagationFullAddress()));
        }
        super.init();
    }

    @Override
    protected NetworkNodeData getNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.TrustScoreNode, nodeIp, serverPort,
                NodeCryptoHelper.getNodeHash());
        networkNodeCrypto.signMessage(networkNodeData);
        return networkNodeData;

    }
}