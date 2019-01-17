package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.database.Interfaces.IRocksDBConnector;
import io.coti.basenode.services.interfaces.INetworkDetailsService;
import io.coti.basenode.crypto.KYCApprovementResponseCrypto;
import io.coti.nodemanager.data.ActiveNodeData;
import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.data.NodeNetworkDataTimestamp;
import io.coti.nodemanager.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.database.NetworkNodeStatus;
import io.coti.nodemanager.model.ActiveNode;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NodeManagementService implements INodeManagementService {

    public static final String FULL_NODES_FORWALLET_KEY = "FullNodes";
    public static final String TRUST_SCORE_NODES_FORWALLET_KEY = "TrustScoreNodes";
    private final IPropagationPublisher propagationPublisher;
    private final NetworkNodeCrypto networkNodeCrypto;
    private final KYCApprovementResponseCrypto kycApprovementResponseCrypto;
    private final NodeHistory nodeHistory;
    private final IRocksDBConnector dataBaseConnector;
    private final ActiveNode activeNode;
    private final INetworkDetailsService networkDetailsService;
    @Value("${propagation.port}")
    private String propagationPort;


    @Autowired
    public NodeManagementService(IPropagationPublisher propagationPublisher, NetworkNodeCrypto networkNodeCrypto,
                                 NodeHistory nodeHistory, IRocksDBConnector dataBaseConnector, ActiveNode activeNode,
                                 KYCApprovementResponseCrypto kycApprovementResponseCrypto, INetworkDetailsService networkDetailsService) {
        this.propagationPublisher = propagationPublisher;
        this.networkNodeCrypto = networkNodeCrypto;
        this.nodeHistory = nodeHistory;
        this.dataBaseConnector = dataBaseConnector;
        this.activeNode = activeNode;
        this.kycApprovementResponseCrypto = kycApprovementResponseCrypto;
        this.networkDetailsService = networkDetailsService;
    }

    @PostConstruct
    private void init() {
        dataBaseConnector.init();
        networkDetailsService.getNetworkDetails().setNodeManagerPropagationAddress("tcp://localhost:" + propagationPort);
        propagationPublisher.init(propagationPort);
    }

    public void updateNetworkChanges() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("An error was thrown", e);
        }
        log.info("Propagating networkDetails after a change: {}", networkDetailsService.getNetworkDetails());
        propagationPublisher.propagate(networkDetailsService.getNetworkDetails(), Arrays.asList(NodeType.FullNode, NodeType.ZeroSpendServer,
                NodeType.DspNode, NodeType.TrustScoreNode));
    }

    public NetworkDetails newNode(NetworkNodeData networkNodeData) throws IllegalAccessException {
        validateNetworkNodeData(networkNodeData);
        if (networkDetailsService.isNodeExistsOnMemory(networkNodeData)) {
            boolean isUpdated = networkDetailsService.updateNetworkNode(networkNodeData);
            if (isUpdated) {
                ActiveNodeData activeNodeData = activeNode.getByHash(networkNodeData.getHash());
                if (activeNodeData == null) {
                    log.error("Node {} wasn't found in activeNode table but was found in memory!");
                }
            }
        } else {
            networkDetailsService.addNode(networkNodeData);
        }
        ActiveNodeData activeNodeData = new ActiveNodeData(networkNodeData.getHash(), networkNodeData);
        activeNode.put(activeNodeData);
        insertToDB(networkNodeData, NetworkNodeStatus.ACTIVE);
        updateNetworkChanges();
        return networkDetailsService.getNetworkDetails();
    }

    private void validateNetworkNodeData(NetworkNodeData networkNodeData) throws IllegalAccessException {
        if (!validateNodeProperties(networkNodeData)) {
            log.error("Illegal networkNodeData properties received: {}", networkNodeData);
            throw new IllegalAccessException("The node " + networkNodeData + "didn't pass validation");
        }
    }

    private boolean validateCCAApprovement(NetworkNodeData networkNodeData) {
//        KYCApprovementResponse kycApprovementResponse = networkNodeData.getKycApprovementResponse();
//        kycApprovementResponse.setSignerHash(new Hash(ccaPublicKey));
//        return ccaApprovementResponseCrypto.verifySignature(kycApprovementResponse);
        return true;
    }

    private boolean validateNodeProperties(NetworkNodeData networkNodeData) {
        boolean isNodeSignatureValid = true;//networkNodeCrypto.verifySignature(networkNodeData);

        if (!isNodeSignatureValid) {
            log.error("Invalid networkNodeData. NetworkNodeData =  {}", networkNodeData);
        }
        return isNodeSignatureValid;
    }

    private void insertToDB(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus) {
        modifyNodeInNodeHistory(networkNodeData, nodeStatus);
    }

    public void insertDeletedNodeRecord(NetworkNodeData networkNodeData) {
        modifyNodeInNodeHistory(networkNodeData, NetworkNodeStatus.INACTIVE);
    }

    private void modifyNodeInNodeHistory(NetworkNodeData networkNodeData, NetworkNodeStatus status) {
        NodeHistoryData dbNode = nodeHistory.getByHash(networkNodeData.getHash());
        NodeNetworkDataTimestamp nodeNetworkDataTimestamp =
                new NodeNetworkDataTimestamp(getUTCnow(), networkNodeData);
        if (dbNode != null) {
            dbNode.setNodeStatus(status);
            log.debug("Node was updated in the db. node: {}", dbNode);
        } else {
            dbNode = new NodeHistoryData(status, networkNodeData.getNodeHash(), networkNodeData.getNodeType());
            log.debug("New node was inserted the db. node: {}", dbNode);
        }
        dbNode.getNodeHistory().add(nodeNetworkDataTimestamp);
        nodeHistory.put(dbNode);
    }


    public LocalDateTime getUTCnow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public Map<String, List<SingleNodeDetailsForWallet>> createNetworkDetailsForWallet() {
        Map<String, List<SingleNodeDetailsForWallet>> networkDetailsForWallet = new HashedMap<>();
        List<SingleNodeDetailsForWallet> fullNodesDetailsForWallet = networkDetailsService.getNetworkDetails().getFullNodeNetworkNodesMap().values().stream()
                .map(this::createSingleNodeDetailsForWallet)
                .collect(Collectors.toList());
        List<SingleNodeDetailsForWallet> trustScoreNodesDetailsForWallet = networkDetailsService.getNetworkDetails().getTrustScoreNetworkNodesMap().values().stream()
                .map(this::createSingleNodeDetailsForWallet)
                .collect(Collectors.toList());
        networkDetailsForWallet.put(FULL_NODES_FORWALLET_KEY, fullNodesDetailsForWallet);
        networkDetailsForWallet.put(TRUST_SCORE_NODES_FORWALLET_KEY, trustScoreNodesDetailsForWallet);
        return networkDetailsForWallet;
    }

    private SingleNodeDetailsForWallet createSingleNodeDetailsForWallet(NetworkNodeData node) {
        if (NodeType.FullNode.equals(node.getNodeType())) {
            return new SingleNodeDetailsForWallet(node.getHash(), node.getHttpFullAddress(), node.getFeePercentage(), node.getTrustScore());
        }
        return new SingleNodeDetailsForWallet(node.getHash(), node.getHttpFullAddress(), null, null);
    }
}