package io.coti.trustscore.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.NetworkNode;
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
public class InitializationService extends BaseNodeInitializationService{

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private INetworkService networkService;

    @Value("${server.port}")
    private String serverPort;
    @Value("${server.ip}")
    private String nodeIp;

    @Autowired
    private IPropagationSubscriber subscriber;

    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;

    @PostConstruct
    public void init() {
        super.connectToNetwork();
        communicationService.initSubscriber(NodeType.TrustScoreNode);
        List<NetworkNode> dspNetworkNodes = this.networkService.getNetworkData().getDspNetworkNodes();
        Collections.shuffle(dspNetworkNodes);
        NetworkNode zerospendNetworkNode = this.networkService.getNetworkData().getZerospendServer();
        if(zerospendNetworkNode != null ) {
            networkService.setRecoveryServerAddress(zerospendNetworkNode.getHttpFullAddress());
        }
        if(dspNetworkNodes.size() > 0){
                dspNetworkNodes.forEach(dspnode -> subscriber.connectAndSubscribeToServer(dspnode.getPropagationFullAddress()));
        }
        if(zerospendNetworkNode != null ) {
            subscriber.connectAndSubscribeToServer(zerospendNetworkNode.getPropagationFullAddress());
        }
        super.init();

    }

    @Override
    protected NetworkNode getNodeProperties() {
        NetworkNode networkNode = new NetworkNode(NodeType.TrustScoreNode, nodeIp, serverPort,
                NodeCryptoHelper.getNodeHash());
        networkNodeCrypto.signMessage(networkNode);
        return networkNode;

    }
}