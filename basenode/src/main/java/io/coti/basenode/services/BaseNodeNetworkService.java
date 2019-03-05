package io.coti.basenode.services;

import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_NODE_REGISTRAR;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_NODE_REGISTRATION_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;


@Slf4j
@Service
public class BaseNodeNetworkService implements INetworkService {

    protected String recoveryServerAddress;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;
    private String nodeManagerPropagationAddress;
    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;
    @Autowired
    private NodeRegistrationCrypto nodeRegistrationCrypto;
    private Map<NodeType, Map<Hash, NetworkNodeData>> multipleNodeMaps;
    private Map<NodeType, NetworkNodeData> singleNodeNetworkDataMap;


    @Override
    public void init() {
        multipleNodeMaps = new EnumMap<>(NodeType.class);
        NodeTypeService.getNodeTypeList(true).forEach(nodeType -> multipleNodeMaps.put(nodeType, new ConcurrentHashMap<>()));

        singleNodeNetworkDataMap = new EnumMap<>(NodeType.class);
        NodeTypeService.getNodeTypeList(false).forEach(nodeType -> singleNodeNetworkDataMap.put(nodeType, null));

    }

    public void handleNetworkChanges(NetworkData networkData) {
    }

    public String getRecoveryServerAddress() {
        return recoveryServerAddress;
    }

    public void setRecoveryServerAddress(String recoveryServerAddress) {
        this.recoveryServerAddress = recoveryServerAddress;
    }

    public Map<Hash, NetworkNodeData> getMapFromFactory(NodeType nodeType) {
        Map<Hash, NetworkNodeData> mapToGet = multipleNodeMaps.get(nodeType);
        if (mapToGet == null) {
            log.error("Unsupported networkNodeData type : {}", nodeType);
            throw new IllegalArgumentException("Unsupported networkNodeData type");
        }
        return mapToGet;
    }

    @Override
    public NetworkNodeData getSingleNodeData(NodeType nodeType) {
        if (!singleNodeNetworkDataMap.containsKey(nodeType)) {
            log.error("Unsupported networkNodeData type : {}", nodeType);
            throw new IllegalArgumentException("Unsupported networkNodeData type");
        }
        return singleNodeNetworkDataMap.get(nodeType);
    }

    private void setSingleNodeData(NodeType nodeType, NetworkNodeData newNetworkNodeData) {
        if (!singleNodeNetworkDataMap.containsKey(nodeType)) {
            log.error("Unsupported networkNodeData type : {}", nodeType);
            throw new IllegalArgumentException("Unsupported networkNodeData type");
        }
        if(newNetworkNodeData != null && !newNetworkNodeData.getNodeType().equals(nodeType)) {
            log.error("Invalid networkNodeData type : {}", nodeType);
            throw new IllegalArgumentException("Invalid networkNodeData type");
        }
        singleNodeNetworkDataMap.put(nodeType, newNetworkNodeData);
    }

    @Override
    public void addNode(NetworkNodeData networkNodeData) {
        try {
            if(networkNodeData.getNodeHash() == null || networkNodeData.getNodeType() == null) {
                log.error("Invalid networkNodeData adding request");
                throw new IllegalArgumentException("Invalid networkNodeData adding request");
            }
            if (!NodeTypeService.valueOf(networkNodeData.getNodeType().toString()).isMultipleNode()) {
                setSingleNodeData(networkNodeData.getNodeType(),networkNodeData);
            } else {
                getMapFromFactory(networkNodeData.getNodeType()).putIfAbsent(networkNodeData.getHash(), networkNodeData);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeNode(NetworkNodeData networkNodeData) {
        try {
            if (!NodeTypeService.valueOf(networkNodeData.getNodeType().toString()).isMultipleNode()) {
                setSingleNodeData(networkNodeData.getNodeType(), null);
            } else {
                if (getMapFromFactory(networkNodeData.getNodeType()).remove(networkNodeData.getHash()) == null) {
                    log.info("NetworkNode {} of type {} isn't found", networkNodeData.getNodeHash(), networkNodeData.getNodeType());
                    return;
                }
            }
            log.info("NetworkNode {}  of type {} is deleted", networkNodeData.getNodeHash(), networkNodeData.getNodeType());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public boolean updateNetworkNode(NetworkNodeData networkNodeData) {
        NetworkNodeData node = null;
        if (!NodeTypeService.valueOf(networkNodeData.getNodeType().toString()).isMultipleNode()) {
            node = singleNodeNetworkDataMap.get(networkNodeData.getNodeType());
        } else {
            Map<Hash, NetworkNodeData> networkMapToChange = getMapFromFactory(networkNodeData.getNodeType());
            node = networkMapToChange.get(networkNodeData.getNodeHash());
        }
        if (node != null) {
            node.setAddress(networkNodeData.getAddress());
            node.setHttpPort(networkNodeData.getHttpPort());
            node.setReceivingPort(networkNodeData.getReceivingPort());
            node.setRecoveryServerAddress(networkNodeData.getRecoveryServerAddress());
            node.setPropagationPort(networkNodeData.getPropagationPort());
            node.setNodeSignature(networkNodeData.getNodeSignature());
            node.setSignerHash(networkNodeData.getSignerHash());
            return true;
        }
        return false;
    }

    @Override
    public void validateNetworkNodeData(NetworkNodeData networkNodeData) throws Exception {
        if (!networkNodeCrypto.verifySignature(networkNodeData)) {
            log.error("Invalid signature by node {}", networkNodeData.getNodeHash());
            throw new Exception(INVALID_SIGNATURE);
        }
        if (!nodeRegistrationCrypto.verifySignature(networkNodeData.getNodeRegistrationData())) {
            log.error("Invalid node registration signature by node {}", networkNodeData.getNodeHash());
            throw new Exception(INVALID_NODE_REGISTRATION_SIGNATURE);
        }
        if (!networkNodeData.getNodeRegistrationData().getRegistrarHash().toString().equals(kycServerPublicKey)) {
            log.error("Invalid registrar node hash for node {}", networkNodeData.getNodeHash());
            throw new Exception(INVALID_NODE_REGISTRAR);
        }
    }


    @Override
    public boolean isNodeExistsOnMemory(NetworkNodeData networkNodeData) {
        try {
            if (!NodeTypeService.valueOf(networkNodeData.getNodeType().toString()).isMultipleNode()) {
                return singleNodeNetworkDataMap.containsKey(networkNodeData.getNodeType()) &&
                       singleNodeNetworkDataMap.get(networkNodeData.getNodeType()) != null &&
                       singleNodeNetworkDataMap.get(networkNodeData.getNodeType()).getNodeHash().equals(networkNodeData.getNodeHash());
            } else {
                return getMapFromFactory(networkNodeData.getNodeType()).containsKey(networkNodeData.getHash()) &&
                        getMapFromFactory(networkNodeData.getNodeType()).get(networkNodeData.getHash()).getNodeHash().equals(networkNodeData.getNodeHash());
            }
        } catch(NullPointerException e) {
            return false;
        }
    }

    @Override
    public List<NetworkNodeData> getShuffledNetworkNodeDataListFromMapValues(NodeType nodeType) {
        try {
            Map<Hash, NetworkNodeData> dataMap = getMapFromFactory(nodeType);
            List<NetworkNodeData> nodeDataList = new LinkedList<>(dataMap.values());
            Collections.shuffle(nodeDataList);
            return nodeDataList;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<NetworkNodeData> getNetworkNodeDataList() {
        List<NetworkNodeData> networkNodeDataList = new ArrayList<>();
        multipleNodeMaps.forEach((nodeType, hashNetworkNodeDataMap) -> hashNetworkNodeDataMap.forEach(((hash, networkNodeData) -> networkNodeDataList.add(networkNodeData))));
        singleNodeNetworkDataMap.forEach(((nodeType, networkNodeData) -> {
            if (networkNodeData != null && networkNodeData.getNodeHash() != null) {
                networkNodeDataList.add(networkNodeData);
            }
        }));
        return networkNodeDataList;
    }

    @Override
    public void addListToSubscriptionAndNetwork(Collection<NetworkNodeData> nodeDataList) {
        Iterator<NetworkNodeData> nodeDataIterator = nodeDataList.iterator();
        while (nodeDataIterator.hasNext()) {
            NetworkNodeData node = nodeDataIterator.next();
            log.info("{} {} is about to be added to subscription and network", node.getNodeType(), node.getHttpFullAddress());
            addNode(node);
            communicationService.addSubscription(node.getPropagationFullAddress());
        }
    }

    @Override
    public void setNodeManagerPropagationAddress(String nodeManagerPropagationAddress) {
        this.nodeManagerPropagationAddress = nodeManagerPropagationAddress;
    }

    @Override
    public String getNodeManagerPropagationAddress() {
        return nodeManagerPropagationAddress;
    }

    @Override
    public NetworkData getNetworkData() {
        NetworkData networkData = new NetworkData();
        networkData.setMultipleNodeMaps(multipleNodeMaps);
        networkData.setSingleNodeNetworkDataMap(singleNodeNetworkDataMap);
        return networkData;
    }

    @Override
    public void setNetworkData(NetworkData networkData) {
        multipleNodeMaps = networkData.getMultipleNodeMaps();
        singleNodeNetworkDataMap = networkData.getSingleNodeNetworkDataMap();
    }

    public void shutdown() {
        log.error("Shutdown All Resources");
    }


}
