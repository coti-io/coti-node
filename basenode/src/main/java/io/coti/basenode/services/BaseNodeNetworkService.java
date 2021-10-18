package io.coti.basenode.services;

import io.coti.basenode.communication.ZeroMQSubscriberQueue;
import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.crypto.NetworkCrypto;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.crypto.NodeRegistrationCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.NetworkChangeException;
import io.coti.basenode.exceptions.NetworkException;
import io.coti.basenode.exceptions.NetworkNodeValidationException;
import io.coti.basenode.http.CustomHttpComponentsClientHttpRequestFactory;
import io.coti.basenode.services.interfaces.ICommunicationService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ISslService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;


@Slf4j
@Service
public class BaseNodeNetworkService implements INetworkService {

    protected String recoveryServerAddress;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;
    @Value("${node.manager.public.key:}")
    private String nodeManagerPublicKey;
    @Value("${network}")
    protected NetworkType networkType;
    @Value("${validate.server.url:true}")
    private boolean validateServerUrl;
    private String nodeManagerPropagationAddress;
    private String connectToNetworkUrl;
    @Autowired
    private ICommunicationService communicationService;
    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;
    @Autowired
    private NodeRegistrationCrypto nodeRegistrationCrypto;
    @Autowired
    private NetworkCrypto networkCrypto;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    @Autowired
    private ISslService sslService;
    protected Map<NodeType, Map<Hash, NetworkNodeData>> multipleNodeMaps;
    protected Map<NodeType, NetworkNodeData> singleNodeNetworkDataMap;
    protected NetworkNodeData networkNodeData;

    @Override
    public void init() {
        multipleNodeMaps = new EnumMap<>(NodeType.class);
        NodeTypeService.getNodeTypeList(true).forEach(nodeType -> multipleNodeMaps.put(nodeType, new ConcurrentHashMap<>()));

        singleNodeNetworkDataMap = new EnumMap<>(NodeType.class);
        NodeTypeService.getNodeTypeList(false).forEach(nodeType -> singleNodeNetworkDataMap.put(nodeType, null));

        sslService.init();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 10000)
    public void lastState() {
        try {
            if (multipleNodeMaps != null) {
                log.info("FullNode: {}, DspNode: {}, TrustScoreNode: {}", multipleNodeMaps.get(NodeType.FullNode).size(), multipleNodeMaps.get(NodeType.DspNode).size(), multipleNodeMaps.get(NodeType.TrustScoreNode).size());
            }
        } catch (Exception e) {
            log.error("Error at last state of network", e);
        }
    }

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        log.info("New network structure received");

        verifyNodeManager(newNetworkData);

        if (propagationSubscriber.getMessageQueueSize(ZeroMQSubscriberQueue.NETWORK) != 0) {
            throw new NetworkChangeException("Skipped handling network data due to pending newer network changes");
        }

        if (!isNodeConnectedToNetwork(newNetworkData)) {
            try {
                connectToNetwork();
            } catch (NetworkException e) {
                e.logMessage();
                System.exit(SpringApplication.exit(applicationContext));
            }
        }
    }

    @Override
    public void verifyNodeManager(NetworkData newNetworkData) {
        if (!verifyNodeManagerKey(newNetworkData)) {
            throw new NetworkNodeValidationException("Invalid node manager hash");
        }

        if (!networkCrypto.verifySignature(newNetworkData)) {
            throw new NetworkNodeValidationException("Invalid signature by node manager");
        }
    }

    private boolean verifyNodeManagerKey(NetworkData newNetworkData) {
        return nodeManagerPublicKey.equals(newNetworkData.getSignerHash().toString());
    }

    public boolean isNodeConnectedToNetwork(NetworkData networkData) {
        log.debug("{} is connected to network", networkData);
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
            throw new IllegalArgumentException(String.format(INVALID_NODE_TYPE, nodeType));
        }
        return mapToGet;
    }

    @Override
    public NetworkNodeData getSingleNodeData(NodeType nodeType) {
        if (!singleNodeNetworkDataMap.containsKey(nodeType)) {
            throw new IllegalArgumentException(String.format(INVALID_NODE_TYPE, nodeType));
        }
        return singleNodeNetworkDataMap.get(nodeType);
    }

    private void setSingleNodeData(NodeType nodeType, NetworkNodeData newNetworkNodeData) {
        if (!singleNodeNetworkDataMap.containsKey(nodeType)) {
            throw new IllegalArgumentException(String.format(INVALID_NODE_TYPE, nodeType));
        }
        if (newNetworkNodeData != null && !newNetworkNodeData.getNodeType().equals(nodeType)) {
            log.error("Invalid networkNodeData type : {}", nodeType);
            throw new IllegalArgumentException("Invalid networkNodeData type");
        }
        singleNodeNetworkDataMap.put(nodeType, newNetworkNodeData);
    }

    @Override
    public void addNode(NetworkNodeData networkNodeData) {

        if (networkNodeData.getNodeHash() == null || networkNodeData.getNodeType() == null) {
            log.error("Invalid networkNodeData adding request");
            throw new IllegalArgumentException("Invalid networkNodeData adding request");
        }
        if (!NodeTypeService.getByNodeType(networkNodeData.getNodeType()).isMultipleNode()) {
            setSingleNodeData(networkNodeData.getNodeType(), networkNodeData);
        } else {
            getMapFromFactory(networkNodeData.getNodeType()).put(networkNodeData.getHash(), networkNodeData);
        }

    }

    @Override
    public void removeNode(NetworkNodeData networkNodeData) {
        if (!NodeTypeService.getByNodeType(networkNodeData.getNodeType()).isMultipleNode()) {
            setSingleNodeData(networkNodeData.getNodeType(), null);
        } else {
            if (getMapFromFactory(networkNodeData.getNodeType()).remove(networkNodeData.getHash()) == null) {
                log.info("NetworkNode {} of type {} isn't found", networkNodeData.getNodeHash(), networkNodeData.getNodeType());
                return;
            }
        }
        log.info("NetworkNode {}  of type {} is deleted", networkNodeData.getNodeHash(), networkNodeData.getNodeType());
    }

    @Override
    public void validateNetworkNodeData(NetworkNodeData networkNodeData) {
        if (!networkNodeData.getNetworkType().equals(networkType)) {
            log.error("Invalid network type {} by node {}", networkNodeData.getNetworkType(), networkNodeData.getNodeHash());
            throw new NetworkNodeValidationException(String.format(INVALID_NETWORK_TYPE, networkType, networkNodeData.getNetworkType()));
        }

        if (!networkNodeCrypto.verifySignature(networkNodeData)) {
            log.error("Invalid signature by node {}", networkNodeData.getNodeHash());
            throw new NetworkNodeValidationException(INVALID_SIGNATURE);
        }

        if (!nodeRegistrationCrypto.verifySignature(networkNodeData.getNodeRegistrationData())) {
            log.error("Invalid node registration signature by node {}", networkNodeData.getNodeHash());
            throw new NetworkNodeValidationException(INVALID_NODE_REGISTRATION_SIGNATURE);
        }

        if (!networkNodeData.getNodeRegistrationData().getRegistrarHash().toString().equals(kycServerPublicKey)) {
            log.error("Invalid registrar node hash for node {}", networkNodeData.getNodeHash());
            throw new NetworkNodeValidationException(INVALID_NODE_REGISTRAR);
        }

        if (!validateAddress(networkNodeData)) {
            log.error("Invalid IP, expected IPV4, received ip: {}", networkNodeData.getAddress());
            throw new NetworkNodeValidationException(INVALID_NODE_IP_VERSION);
        }

        if (networkNodeData.getNodeType().equals(NodeType.FullNode) && validateServerUrl) {
            validateWebServerUrl(networkNodeData);
        }

        if (networkNodeData.getNodeType().equals(NodeType.FullNode) && !validateFeeData(networkNodeData.getFeeData())) {
            log.error("Invalid fee data for full node {}", networkNodeData.getNodeHash());
            throw new NetworkNodeValidationException(INVALID_FULL_NODE_FEE);
        }
    }

    private boolean validateAddress(NetworkNodeData networkNodeData) {
        InetAddressValidator validator = InetAddressValidator.getInstance();
        String ip = networkNodeData.getAddress();
        return validator.isValidInet4Address(ip);
    }

    private void validateWebServerUrl(NetworkNodeData networkNodeData) {
        String ip = networkNodeData.getAddress();

        String webServerUrl = networkNodeData.getWebServerUrl();
        URL url = getUrl(webServerUrl);

        validateWebServerUrl(url, ip);
        validateSSL(url, networkNodeData.getNodeHash());
    }

    private void validateWebServerUrl(URL nodeUrl, String ip) {

        String protocol = nodeUrl.getProtocol();
        if (!protocol.equals("https")) {
            throw new NetworkNodeValidationException(String.format(INVALID_NODE_SERVER_URL_SSL_REQUIRED, nodeUrl));
        }

        String host = nodeUrl.getHost();
        if (host.isEmpty()) {
            throw new NetworkNodeValidationException(String.format(INVALID_NODE_SERVER_URL_EMPTY_HOST, nodeUrl));
        }
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(host);
        } catch (Exception e) {
            throw new NetworkNodeValidationException(String.format(INVALID_NODE_SERVER_URL_UNKNOWN_HOST, nodeUrl), e);
        }
        String expectedIp = inetAddress.getHostAddress();
        if (!expectedIp.equals(ip)) {
            throw new NetworkNodeValidationException(String.format(INVALID_NODE_IP_FOR_SERVER_URL, nodeUrl, host, ip, expectedIp));
        }
    }

    private void validateSSL(URL nodeUrl, Hash nodeHash) {
        HttpsURLConnection conn;
        try {
            conn = (HttpsURLConnection) nodeUrl.openConnection();
            conn.connect();
        } catch (IOException e) {
            throw new NetworkNodeValidationException(String.format(INVALID_NODE_SERVER_URL_SSL_CONNECTION_NOT_OPENED, nodeUrl));
        }
        try {
            Certificate[] certs = conn.getServerCertificates();
            if (certs == null || certs.length == 0) {
                throw new NetworkNodeValidationException(String.format(INVALID_NODE_SERVER_URL_SSL_CERTIFICATE_NOT_FOUND, nodeUrl));
            }
            LocalDate now = LocalDate.now(ZoneId.of("UTC"));
            long daysBeforeExpiration = 0;
            for (Certificate c : certs) {
                if (!(c instanceof X509Certificate)) {
                    throw new NetworkNodeValidationException(String.format(INVALID_NODE_SERVER_URL_SSL_INVALID_SERVER_CERTIFICATE, nodeUrl));
                }
            }

            X509Certificate[] serverCertificates = (X509Certificate[]) certs;
            X509Certificate principalCertificate = serverCertificates[0];
            Date expiresOn = principalCertificate.getNotAfter();
            LocalDate expireDate = expiresOn.toInstant().atZone(ZoneId.of("UTC")).toLocalDate();
            if (!expireDate.isAfter(now)) {
                throw new NetworkNodeValidationException(String.format(INVALID_NODE_SERVER_URL_SSL_FAILED_TO_VERIFY_CERTIFICATE_EXPIRATION, nodeUrl));
            }

            sslService.checkServerTrusted((X509Certificate[]) certs);

            daysBeforeExpiration = now.until(expireDate, ChronoUnit.DAYS);
            log.info("Trusted SSL certificate will expire on : {},  {} days to go for node url {} and node hash {}", expireDate, daysBeforeExpiration, nodeUrl, nodeHash);

        } catch (SSLPeerUnverifiedException e) {
            throw new NetworkNodeValidationException(String.format(INVALID_NODE_SERVER_URL_SSL_FAILED_TO_VERIFY_CERTIFICATE, nodeUrl));
        } finally {
            conn.disconnect();
        }
    }

    @Override
    public String getHost(String webServerUrl) {
        return getUrl(webServerUrl).getHost();
    }

    private URL getUrl(String webServerUrl) {
        try {
            return new URL(webServerUrl);
        } catch (MalformedURLException e) {
            log.error("Node registration requires a valid URL address: {}", webServerUrl);
            throw new NetworkNodeValidationException(INVALID_NODE_SERVER_URL, e);
        }
    }

    @Override
    public boolean validateFeeData(FeeData feeData) {
        return feeData.getFeePercentage().compareTo(new BigDecimal("0")) >= 0 && feeData.getFeePercentage().compareTo(new BigDecimal("100")) < 100 &&
                feeData.getMinimumFee().compareTo(feeData.getMaximumFee()) <= 0;
    }

    @Override
    public List<NetworkNodeData> getShuffledNetworkNodeDataListFromMapValues(NodeType nodeType) {

        try {
            Map<Hash, NetworkNodeData> dataMap = getMapFromFactory(nodeType);
            List<NetworkNodeData> nodeDataList = new LinkedList<>(dataMap.values());
            Collections.shuffle(nodeDataList);
            return nodeDataList;
        } catch (IllegalArgumentException e) {
            log.error("Get shuffled network node list from map values", e);
            return new LinkedList<>();
        }
    }

    @Override
    public List<NetworkNodeData> getNetworkNodeDataList() {
        List<NetworkNodeData> networkNodeDataList = new ArrayList<>();
        multipleNodeMaps.forEach((nodeType, nodeHashToNodeInNetworkMap) -> nodeHashToNodeInNetworkMap.forEach(((nodeHash, nodeInNetwork) -> networkNodeDataList.add(nodeInNetwork))));
        singleNodeNetworkDataMap.forEach(((nodeType, nodeInNetwork) -> {
            if (nodeInNetwork != null && nodeInNetwork.getNodeHash() != null) {
                networkNodeDataList.add(nodeInNetwork);
            }
        }));
        return networkNodeDataList;
    }

    @Override
    public void addListToSubscription(Collection<NetworkNodeData> nodeDataList) {
        for (NetworkNodeData node : nodeDataList) {
            log.info("{} {} is about to be added to subscription and network", node.getNodeType(), node.getHttpFullAddress());
            communicationService.addSubscription(node.getPropagationFullAddress(), node.getNodeType());
        }
    }

    @Override
    public void handleConnectedDspNodesChange(List<NetworkNodeData> connectedDspNodes, Map<Hash, NetworkNodeData> newDspNodeMap, NodeType nodeType) {
        connectedDspNodes.removeIf(dspNode -> {
            boolean remove = !(newDspNodeMap.containsKey(dspNode.getNodeHash()) && newDspNodeMap.get(dspNode.getNodeHash()).getAddress().equals(dspNode.getAddress()));
            if (remove) {
                handleConnectedDspNodeRemove(dspNode, nodeType);
            } else {
                NetworkNodeData newDspNode = newDspNodeMap.get(dspNode.getNodeHash());
                if (!newDspNode.getPropagationPort().equals(dspNode.getPropagationPort())) {
                    communicationService.removeSubscription(dspNode.getPropagationFullAddress(), NodeType.DspNode);
                    communicationService.addSubscription(newDspNode.getPropagationFullAddress(), NodeType.DspNode);
                }
                if (nodeType.equals(NodeType.FullNode) && !newDspNode.getReceivingPort().equals(dspNode.getReceivingPort())) {
                    communicationService.removeSender(dspNode.getReceivingFullAddress(), NodeType.DspNode);
                    communicationService.addSender(newDspNode.getReceivingFullAddress(), NodeType.DspNode);
                }
                if (recoveryServerAddress != null && recoveryServerAddress.equals(dspNode.getHttpFullAddress()) && !newDspNode.getHttpFullAddress().equals(dspNode.getHttpFullAddress())) {
                    recoveryServerAddress = newDspNode.getHttpFullAddress();
                }
                dspNode.clone(newDspNode);
            }
            return remove;
        });

    }

    private void handleConnectedDspNodeRemove(NetworkNodeData dspNode, NodeType nodeType) {
        communicationService.removeSubscription(dspNode.getPropagationFullAddress(), NodeType.DspNode);
        if (nodeType.equals(NodeType.FullNode)) {
            communicationService.removeSender(dspNode.getReceivingFullAddress(), NodeType.DspNode);
        }
        if (recoveryServerAddress != null && recoveryServerAddress.equals(dspNode.getHttpFullAddress())) {
            recoveryServerAddress = null;
        }
    }

    @Override
    public void handleConnectedSingleNodeChange(NetworkData newNetworkData, NodeType singleNodeType, NodeType connectingNodeType) {
        NetworkNodeData newSingleNodeData = newNetworkData.getSingleNodeNetworkDataMap().get(singleNodeType);
        NetworkNodeData currentSingleNodeData = getSingleNodeData(singleNodeType);
        if (newSingleNodeData != null) {
            if (newSingleNodeData.getPropagationPort() != null) {
                handleSingleNodeWithDifferentPropagationPort(singleNodeType, newSingleNodeData, currentSingleNodeData);
            }
            handleConnectedZeroSpendServer(singleNodeType, connectingNodeType, newSingleNodeData, currentSingleNodeData);
            if (recoveryServerAddress != null && (currentSingleNodeData == null || recoveryServerAddress.equals(currentSingleNodeData.getHttpFullAddress()))) {
                recoveryServerAddress = newSingleNodeData.getHttpFullAddress();
            }
            setSingleNodeData(singleNodeType, newSingleNodeData);
        }
    }

    private void handleSingleNodeWithDifferentPropagationPort(NodeType singleNodeType, NetworkNodeData newSingleNodeData, NetworkNodeData currentSingleNodeData) {
        if (currentSingleNodeData != null && currentSingleNodeData.getPropagationPort() != null &&
                !(newSingleNodeData.getPropagationPort().equals(currentSingleNodeData.getPropagationPort()) && newSingleNodeData.getAddress().equals(currentSingleNodeData.getAddress()))) {
            communicationService.removeSubscription(currentSingleNodeData.getPropagationFullAddress(), singleNodeType);
            communicationService.addSubscription(newSingleNodeData.getPropagationFullAddress(), singleNodeType);
        }
        if (currentSingleNodeData == null) {
            communicationService.addSubscription(newSingleNodeData.getPropagationFullAddress(), singleNodeType);
        }
    }

    private void handleConnectedZeroSpendServer(NodeType singleNodeType, NodeType connectingNodeType, NetworkNodeData newSingleNodeData, NetworkNodeData currentSingleNodeData) {
        if (singleNodeType.equals(NodeType.ZeroSpendServer) && connectingNodeType.equals(NodeType.DspNode) && newSingleNodeData.getReceivingPort() != null) {
            if (currentSingleNodeData != null && currentSingleNodeData.getReceivingPort() != null &&
                    !(newSingleNodeData.getReceivingPort().equals(currentSingleNodeData.getReceivingPort()) && newSingleNodeData.getAddress().equals(currentSingleNodeData.getAddress()))) {
                communicationService.removeSender(newSingleNodeData.getReceivingFullAddress(), singleNodeType);
                communicationService.addSender(newSingleNodeData.getReceivingFullAddress(), singleNodeType);
            }
            if (currentSingleNodeData == null) {
                communicationService.addSender(newSingleNodeData.getReceivingFullAddress(), singleNodeType);
            }
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
    public NetworkData getSignedNetworkData() {
        NetworkData networkData = getNetworkData();
        networkCrypto.signMessage(networkData);
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
        try {
            log.info("Connecting to Coti network");
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.setRequestFactory(new CustomHttpComponentsClientHttpRequestFactory());

            HttpEntity<NetworkNodeData> entity = new HttpEntity<>(networkNodeData);
            ResponseEntity<String> response = restTemplate.exchange(connectToNetworkUrl, HttpMethod.PUT, entity, String.class);
            log.info("{}", response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new NetworkException(String.format("Connect to network failed. Node manager response: %s", e.getResponseBodyAsString()));
        } catch (Exception e) {
            throw new NetworkException("Connect to network failed.", e);
        }
    }

    @Override
    public boolean isZeroSpendServerInNetwork() {
        return Optional.ofNullable(getSingleNodeData(NodeType.ZeroSpendServer)).isPresent();
    }

}
