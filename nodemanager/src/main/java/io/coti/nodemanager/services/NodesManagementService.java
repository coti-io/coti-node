package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.nodemanager.data.NetworkDetailsForWallet;
import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.data.NodeNetworkDataTimestamp;
import io.coti.nodemanager.database.NetworkNodeStatus;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.interfaces.INodesManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NodesManagementService implements INodesManagementService {

    private NetworkDetails networkDetails;

    @Autowired
    private IPropagationPublisher propagationPublisher;

    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;

    @Autowired
    private NodeHistory nodeHistory;

    @Value("${propagation.port}")
    private String propagationPort;
    @Autowired
    private IDatabaseConnector dataBaseConnector;


    @PostConstruct
    public void init() {
        networkDetails = new NetworkDetails();
        dataBaseConnector.init();
        networkDetails.setNodeManagerPropagationAddress("tcp://localhost:" + propagationPort);
        propagationPublisher.init(propagationPort);
    }

    public void updateNetworkChanges() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("An error was thrown", e);
        }
        log.info("Propagating networkDetails after a change: {}", networkDetails);
        propagationPublisher.propagate(networkDetails, Arrays.asList(NodeType.FullNode, NodeType.ZeroSpendServer,
                NodeType.DspNode, NodeType.TrustScoreNode));
    }

    public NetworkDetails newNode(NetworkNodeData networkNodeData) throws IllegalAccessException {
        log.info("New networkNodeData received: {}", networkNodeData);
        if (!validateNodeProperties(networkNodeData)) {
            log.error("Illegal networkNodeData properties received: {}", networkNodeData);
            throw new IllegalAccessException("The node " + networkNodeData + "didn't pass validation");
        }
        if(networkDetails.nodeExists(networkNodeData)){
            networkDetails.updateNetworkNode(networkNodeData);
        }
        insertToDB(networkNodeData);
        this.networkDetails.addNode(networkNodeData);
        updateNetworkChanges();
        return networkDetails;
    }

    private boolean validateNodeProperties(NetworkNodeData networkNodeData) {
        boolean isNodeSignatureValid = networkNodeCrypto.verifySignature(networkNodeData);

        if (!isNodeSignatureValid) {
            log.error("Invalid networkNodeData. NetworkNodeData =  {}", networkNodeData);
        }
        return isNodeSignatureValid;
    }

    public NetworkDetails getAllNetworkData() {
        return networkDetails;
    }


    private void insertToDB(NetworkNodeData networkNodeData) {
        modifyNode(networkNodeData, NetworkNodeStatus.CHANGED);
    }

    public void insertDeletedNodeRecord(NetworkNodeData networkNodeData) {
        modifyNode(networkNodeData, NetworkNodeStatus.REMOVED);
    }

    private void modifyNode(NetworkNodeData networkNodeData, NetworkNodeStatus status){
        NodeHistoryData dbNode = nodeHistory.getByHash(networkNodeData.getHash());
        if (dbNode != null) {
            dbNode.setNodeStatus(status);
            NodeNetworkDataTimestamp nodeNetworkDataTimestamp =
                    new NodeNetworkDataTimestamp(getUTCnow(), networkNodeData);
            dbNode.getNodeHistory().add(nodeNetworkDataTimestamp);
            log.debug("Node was updated in the db. node: {}", dbNode);
        } else {
            if(NetworkNodeStatus.REMOVED.equals(status)){
                dbNode = new NodeHistoryData(NetworkNodeStatus.REMOVED, networkNodeData.getNodeHash(), networkNodeData.getNodeType());
            }else{
                dbNode = new NodeHistoryData(NetworkNodeStatus.NEW, networkNodeData.getNodeHash(), networkNodeData.getNodeType());
            }
            dbNode.getNodeHistory().add(new NodeNetworkDataTimestamp(getUTCnow(), networkNodeData));
            log.debug("New node was inserted the db. node: {}", dbNode);
        }
        nodeHistory.put(dbNode);
    }


    private LocalDateTime getUTCnow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public NetworkDetailsForWallet createNetworkDetailsForWallet() {
        NetworkDetailsForWallet networkDetailsForWallet = new NetworkDetailsForWallet();
        networkDetailsForWallet.setDsps(networkDetails.getDspNetworkNodesList());
        networkDetailsForWallet.setTrustScores(networkDetails.getTrustScoreNetworkNodesList());
        return networkDetailsForWallet;
    }

}