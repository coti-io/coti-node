package io.coti.basenode.services;

import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.CustomHttpComponentsClientHttpRequestFactory;
import io.coti.basenode.services.interfaces.ICommunicationService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;


@Slf4j
@Service
public class BaseNodeNetworkService implements INetworkService {

    protected String recoveryServerAddress;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;
    @Value("${network}")
    protected NetworkType networkType;
    private String nodeManagerPropagationAddress;
    private String connectToNetworkUrl;
    @Autowired
    private ICommunicationService communicationService;
    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;
    @Autowired
    private NodeRegistrationCrypto nodeRegistrationCrypto;
    protected Map<NodeType, Map<Hash, NetworkNodeData>> multipleNodeMaps;
    protected Map<NodeType, NetworkNodeData> singleNodeNetworkDataMap;
    protected NetworkNodeData networkNodeData;

    @Override
    public void init() {
        multipleNodeMaps = new EnumMap<>(NodeType.class);
        NodeTypeService.getNodeTypeList(true).forEach(nodeType -> multipleNodeMaps.put(nodeType, new ConcurrentHashMap<>()));

        singleNodeNetworkDataMap = new EnumMap<>(NodeType.class);
        NodeTypeService.getNodeTypeList(false).forEach(nodeType -> singleNodeNetworkDataMap.put(nodeType, null));

    }

    @Scheduled(initialDelay = 1000, fixedDelay = 10000)
    public void lastState() {
        try {
            if (multipleNodeMaps != null) {
                log.info("FullNode: {}, DspNode: {}, TrustScoreNode: {}", multipleNodeMaps.get(NodeType.FullNode).size(), multipleNodeMaps.get(NodeType.DspNode).size(), multipleNodeMaps.get(NodeType.TrustScoreNode).size());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        log.info("New network structure received");

        if (!isNodeConnectedToNetwork(newNetworkData)) {
            connectToNetwork();
        }
    }

    public boolean isNodeConnectedToNetwork(NetworkData networkData) {
        return true;
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
        if (newNetworkNodeData != null && !newNetworkNodeData.getNodeType().equals(nodeType)) {
            log.error("Invalid networkNodeData type : {}", nodeType);
            throw new IllegalArgumentException("Invalid networkNodeData type");
        }
        singleNodeNetworkDataMap.put(nodeType, newNetworkNodeData);
    }

    @Override
    public void addNode(NetworkNodeData networkNodeData) {
        try {
            if (networkNodeData.getNodeHash() == null || networkNodeData.getNodeType() == null) {
                log.error("Invalid networkNodeData adding request");
                throw new IllegalArgumentException("Invalid networkNodeData adding request");
            }
            if (!NodeTypeService.valueOf(networkNodeData.getNodeType().toString()).isMultipleNode()) {
                setSingleNodeData(networkNodeData.getNodeType(), networkNodeData);
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
        if (!networkNodeData.getNetworkType().equals(networkType)) {
            log.error("Invalid network type {} by node {}", networkNodeData.getNetworkType(), networkNodeData.getNodeHash());
            throw new Exception(String.format(INVALID_NETWORK_TYPE, networkType, networkNodeData.getNetworkType()));
        }
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
        if (networkNodeData.getNodeType().equals(NodeType.FullNode) && !validateFeeData(networkNodeData.getFeeData())) {
            log.error("Invalid fee data for full node {}", networkNodeData.getNodeHash());
            throw new Exception(INVALID_FULL_NODE_FEE);
        }
    }

    @Override
    public boolean validateFeeData(FeeData feeData) {
        return feeData.getFeePercentage().compareTo(new BigDecimal("0")) >= 0 && feeData.getFeePercentage().compareTo(new BigDecimal("100")) < 100 &&
                feeData.getMinimumFee().compareTo(feeData.getMaximumFee()) <= 0;
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
        } catch (NullPointerException e) {
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
    public void addListToSubscription(Collection<NetworkNodeData> nodeDataList) {
        Iterator<NetworkNodeData> nodeDataIterator = nodeDataList.iterator();
        while (nodeDataIterator.hasNext()) {
            NetworkNodeData node = nodeDataIterator.next();
            log.info("{} {} is about to be added to subscription and network", node.getNodeType(), node.getHttpFullAddress());
            communicationService.addSubscription(node.getPropagationFullAddress(), node.getNodeType());
        }
    }

    @Override
    public void handleConnectedDspNodesChange(List<NetworkNodeData> connectedDspNodes, Map<Hash, NetworkNodeData> newDspNodeMap, NodeType nodeType) {
        connectedDspNodes.removeIf(dspNode -> {
            boolean remove = !(newDspNodeMap.containsKey(dspNode.getNodeHash()) && newDspNodeMap.get(dspNode.getNodeHash()).getAddress().equals(dspNode.getAddress()));
            if (remove) {
                log.info("Disconnecting from dsp {} from subscribing and receiving", dspNode.getAddress());
                communicationService.removeSubscription(dspNode.getPropagationFullAddress(), NodeType.DspNode);
                communicationService.removeSender(dspNode.getReceivingFullAddress(), NodeType.DspNode);
                if (recoveryServerAddress != null && recoveryServerAddress.equals(dspNode.getHttpFullAddress())) {
                    recoveryServerAddress = null;
                }
            } else {
                NetworkNodeData newDspNode = newDspNodeMap.get(dspNode.getNodeHash());
                if (!newDspNode.getPropagationPort().equals(dspNode.getPropagationPort())) {
                    communicationService.removeSubscription(dspNode.getPropagationFullAddress(), NodeType.DspNode);
                    communicationService.addSubscription(newDspNode.getPropagationFullAddress(), NodeType.DspNode);
                }
                if (nodeType.equals(NodeType.FullNode) && !newDspNode.getReceivingPort().equals(dspNode.getReceivingPort())) {
                    communicationService.removeSender(dspNode.getReceivingFullAddress(), NodeType.DspNode);
                    communicationService.addSender(newDspNode.getReceivingFullAddress());
                }
                if (recoveryServerAddress != null && recoveryServerAddress.equals(dspNode.getHttpFullAddress()) && !newDspNode.getHttpFullAddress().equals(dspNode.getHttpFullAddress())) {
                    recoveryServerAddress = newDspNode.getHttpFullAddress();
                }
                dspNode.clone(newDspNode);
            }
            return remove;
        });

    }

    @Override
    public void handleConnectedSingleNodeChange(NetworkData newNetworkData, NodeType singleNodeType, NodeType connectingNodeType) {
        NetworkNodeData newSingleNodeData = newNetworkData.getSingleNodeNetworkDataMap().get(singleNodeType);
        NetworkNodeData singleNodeData = getSingleNodeData(singleNodeType);
        if (newSingleNodeData != null) {
            if (newSingleNodeData.getPropagationPort() != null) {
                if (singleNodeData != null && singleNodeData.getPropagationPort() != null && !newSingleNodeData.getPropagationPort().equals(singleNodeData.getPropagationPort())) {
                    communicationService.removeSubscription(singleNodeData.getPropagationPort(), singleNodeType);
                    communicationService.addSubscription(newSingleNodeData.getPropagationFullAddress(), singleNodeType);
                }
                if (singleNodeData == null) {
                    communicationService.addSubscription(newSingleNodeData.getPropagationFullAddress(), singleNodeType);
                }
            }
            if (singleNodeType.equals(NodeType.ZeroSpendServer) && connectingNodeType.equals(NodeType.DspNode) && newSingleNodeData.getReceivingPort() != null) {
                if (singleNodeData != null && singleNodeData.getReceivingPort() != null && !newSingleNodeData.getReceivingPort().equals(singleNodeData.getReceivingPort())) {
                    communicationService.removeSender(newSingleNodeData.getReceivingFullAddress(), singleNodeType);
                    communicationService.addSender(newSingleNodeData.getReceivingFullAddress());
                }
                if (singleNodeData == null) {
                    communicationService.addSender(newSingleNodeData.getReceivingFullAddress());
                }
            }
            if (recoveryServerAddress != null && (singleNodeData == null || singleNodeData != null && recoveryServerAddress.equals(singleNodeData.getHttpFullAddress()))) {
                recoveryServerAddress = newSingleNodeData.getHttpFullAddress();
            }
            setSingleNodeData(singleNodeType, newSingleNodeData);
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
    public void setConnectToNetworkUrl(String connectToNetworkUrl) {
        this.connectToNetworkUrl = connectToNetworkUrl;
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

    @Override
    public void setNetworkNodeData(NetworkNodeData networkNodeData) {
        this.networkNodeData = networkNodeData;
    }

    @Override
    public NetworkNodeData getNetworkNodeData() {
        return networkNodeData;
    }

    @Override
    public void connectToNetwork() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new CustomHttpComponentsClientHttpRequestFactory());

        HttpEntity<NetworkNodeData> entity = new HttpEntity<>(networkNodeData);
        try {
            ResponseEntity<String> response = restTemplate.exchange(connectToNetworkUrl, HttpMethod.PUT, entity, String.class);
            log.info("{}", response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("Node manager error: ", e.getResponseBodyAsString());
            System.exit(-1);
        }
    }

}
