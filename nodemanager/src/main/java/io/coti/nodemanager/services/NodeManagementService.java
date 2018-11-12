package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.database.Interfaces.IRocksDBConnector;
import io.coti.nodemanager.data.ActiveNodeData;
import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.data.NodeNetworkDataTimestamp;
import io.coti.nodemanager.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.database.NetworkNodeStatus;
import io.coti.nodemanager.model.ActiveNode;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import io.coti.nodemanager.services.interfaces.ITrustScoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NodeManagementService implements INodeManagementService {

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
    private IRocksDBConnector dataBaseConnector;
    @Autowired
    private ITrustScoreService trustScoreService;

    @Autowired
    private ActiveNode activeNode;


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
        if (!validateNodeProperties(networkNodeData)) {
            log.error("Illegal networkNodeData properties received: {}", networkNodeData);
            throw new IllegalAccessException("The node " + networkNodeData + "didn't pass validation");
        }
        if(NodeType.FullNode.equals(networkNodeData.getNodeType())){
            networkNodeData.setTrustScore(trustScoreService.getTrustScore(networkNodeData));
        }
        if (networkDetails.isNodeExistsOnMemory(networkNodeData)) {
            boolean isUpdated = networkDetails.updateNetworkNode(networkNodeData);
            if(isUpdated){
                ActiveNodeData activeNodeData = activeNode.getByHash(networkNodeData.getHash());
                if(activeNodeData == null){
                    log.error("Node {} wasn't found in activeNode table but was found in memory!");
                }
            }
        } else {
            this.networkDetails.addNode(networkNodeData);
        }
        ActiveNodeData activeNodeData = new ActiveNodeData(networkNodeData.getHash(), networkNodeData);
        activeNode.put(activeNodeData);
        insertToDB(networkNodeData, NetworkNodeStatus.ACTIVE);
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


    private void insertToDB(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus) {
        modifyNode(networkNodeData, nodeStatus);
    }

    public void insertDeletedNodeRecord(NetworkNodeData networkNodeData) {
        modifyNode(networkNodeData, NetworkNodeStatus.INACTIVE);
    }

    private void modifyNode(NetworkNodeData networkNodeData, NetworkNodeStatus status) {
        NodeHistoryData dbNode = nodeHistory.getByHash(networkNodeData.getHash());
        if (dbNode != null) {
            dbNode.setNodeStatus(status);
            NodeNetworkDataTimestamp nodeNetworkDataTimestamp =
                    new NodeNetworkDataTimestamp(getUTCnow(), networkNodeData);
            dbNode.getNodeHistory().add(nodeNetworkDataTimestamp);
            log.debug("Node was updated in the db. node: {}", dbNode);
        } else {
            if (NetworkNodeStatus.INACTIVE.equals(status)) {
                dbNode = new NodeHistoryData(NetworkNodeStatus.INACTIVE, networkNodeData.getNodeHash(), networkNodeData.getNodeType());
            } else {
                dbNode = new NodeHistoryData(NetworkNodeStatus.ACTIVE, networkNodeData.getNodeHash(), networkNodeData.getNodeType());
            }
            dbNode.getNodeHistory().add(new NodeNetworkDataTimestamp(getUTCnow(), networkNodeData));
            log.debug("New node was inserted the db. node: {}", dbNode);
        }
        nodeHistory.put(dbNode);
    }


    private LocalDateTime getUTCnow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public Map<String, List<SingleNodeDetailsForWallet>> createNetworkDetailsForWallet() {
        Map<String, List<SingleNodeDetailsForWallet>> networkDetailsForWallet = new HashedMap<>();
        List<SingleNodeDetailsForWallet> fullNodesDetailsForWallet = new LinkedList<>();
        List<SingleNodeDetailsForWallet> trustScoreNodesDetailsForWallet = new LinkedList<>();
        fillNetworkDetailsListForWallet(fullNodesDetailsForWallet, networkDetails.getFullNetworkNodesList());
        fillNetworkDetailsListForWallet(trustScoreNodesDetailsForWallet, networkDetails.getTrustScoreNetworkNodesList());
        networkDetailsForWallet.put("FullNodes", fullNodesDetailsForWallet);
        networkDetailsForWallet.put("TrustScoreNodes", trustScoreNodesDetailsForWallet);
        return networkDetailsForWallet;
    }

    private void fillNetworkDetailsListForWallet(List<SingleNodeDetailsForWallet> detailsForWalletList, List<NetworkNodeData> nodeData) {
        nodeData.forEach(node -> {
                    SingleNodeDetailsForWallet nodeDetails = new SingleNodeDetailsForWallet(node.getHttpFullAddress(), node.getFeePercentage(), node.getTrustScore());
                    detailsForWalletList.add(nodeDetails);
                }
        );
    }
}