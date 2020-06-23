package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeRegistrationData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.exceptions.NetworkNodeValidationException;
import io.coti.basenode.http.GetNetworkVotersRequest;
import io.coti.basenode.services.BaseNodeNetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_NODE_TYPE;

@Slf4j
@Service
public class NetworkService extends BaseNodeNetworkService {

    public Map<String, List<String>> getNetworkSummary() {
        Map<String, List<String>> summaryMap = new HashMap<>();
        createSummaryStringFromNodeList(getMapFromFactory(NodeType.FullNode), summaryMap);
        createSummaryStringFromNodeList(getMapFromFactory(NodeType.DspNode), summaryMap);
        createSummaryStringFromNodeList(getMapFromFactory(NodeType.TrustScoreNode), summaryMap);
        return summaryMap;
    }

    private void createSummaryStringFromNodeList(Map<Hash, NetworkNodeData> networkNodeDataMap, Map<String, List<String>> summaryMap) {
        if (!networkNodeDataMap.isEmpty()) {
            String nodeTypeAsString = networkNodeDataMap.entrySet().iterator().next().getValue().getNodeType().name();
            summaryMap.put(nodeTypeAsString, new LinkedList<String>());
            for (NetworkNodeData nodeData : networkNodeDataMap.values()) {
                summaryMap.get(nodeTypeAsString).add(nodeData.getHttpFullAddress());
            }
        }
    }

    @Override
    public List<Hash> getCurrentValidators() {
        NetworkNodeData zeroSpendNodeData = getSingleNodeData(NodeType.ZeroSpendServer);
        ArrayList<Hash> nodeDataHashes = new ArrayList<>(getMapFromFactory(NodeType.DspNode).keySet());
        if (zeroSpendNodeData != null) {
            nodeDataHashes.add(zeroSpendNodeData.getHash());
        }
        return nodeDataHashes;
    }

    public List<Hash> getCurrentValidatorsZeroSpend(GetNetworkVotersRequest getNetworkVotersRequest) {
        NodeRegistrationData nodeRegistrationData = getNetworkVotersRequest.getNodeRegistrationData();
        if (nodeRegistrationData == null || !nodeRegistrationCrypto.verifySignature(nodeRegistrationData)) {
            throw new NetworkNodeValidationException("Invalid node registration data");
        }
        if (!nodeRegistrationData.getNodeType().equals(NodeType.ZeroSpendServer)) {
            throw new NetworkNodeValidationException(String.format(INVALID_NODE_TYPE, nodeRegistrationData.getNodeType()));
        }
        List<Hash> currentValidators = getCurrentValidators();
        if (!currentValidators.contains(nodeRegistrationData.getNodeHash())) {
            currentValidators.add(nodeRegistrationData.getNodeHash());
        }
        return currentValidators;
    }
}
