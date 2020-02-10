package io.coti.nodemanager.services;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeTrustScoreData;
import io.coti.basenode.data.NodeTrustScoreDataResult;
import io.coti.basenode.http.GetTrustScoreRequest;
import io.coti.basenode.http.GetUserTrustScoreResponse;
import io.coti.basenode.http.data.NodeTrustScoreRequest;
import io.coti.basenode.http.data.NodeTrustScoreResponse;
import io.coti.nodemanager.crypto.NodeTrustScoreRequestCrypto;
import io.coti.nodemanager.services.interfaces.ITrustScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TrustScoreService implements ITrustScoreService {

    private static final String GET_TRUSTSCORE_ENDPOINT = "/usertrustscore";
    private static final int NUM_OF_TRUSTSCORE_NODES = 3;
    public static final String TRUSTSCORE_DATA_ENDPOINT = "/trustscore_data";
    public static final String TRUSTSCORE_AGGREGATION_DATA_ENDPOINT = "/trustscore_aggregation_data";
    @Value("${global.private.key}")
    private String globalPrivateKey;
    private final RestTemplate httpRequest;
    private final NodeTrustScoreRequestCrypto trustScoreRequestCrypto;

    @Autowired
    public TrustScoreService(RestTemplate httpRequest, NodeTrustScoreRequestCrypto trustScoreRequestCrypto) {
        this.httpRequest = httpRequest;
        this.trustScoreRequestCrypto = trustScoreRequestCrypto;
    }

    @Override
    public Double getTrustScore(NetworkNodeData networkNodeData, List<NetworkNodeData> trustScoreNodeList) {
        GetTrustScoreRequest getTrustScoreRequest = new GetTrustScoreRequest();
        getTrustScoreRequest.setUserHash(networkNodeData.getNodeHash());
        ResponseEntity<Object> trustScoreResponse = null;
        for (NetworkNodeData trustScoreNode : trustScoreNodeList) {
            try {
                trustScoreResponse = httpRequest.postForEntity
                        (trustScoreNode.getHttpFullAddress() + GET_TRUSTSCORE_ENDPOINT, getTrustScoreRequest, Object.class);

                if (!HttpStatus.OK.equals(trustScoreResponse.getStatusCode())) {
                    log.error("Trust score node {} returned : {}", trustScoreNode.getHttpFullAddress(), trustScoreResponse);
                    trustScoreResponse = null;
                }
                if (trustScoreResponse == null || trustScoreResponse.getBody() == null) {
                    log.error("Trust score node {} returned null", trustScoreNode.getHttpFullAddress());
                    trustScoreResponse = null;
                } else {
                    log.info("Response from node: {} . TrustScoreNode: {}", trustScoreResponse, trustScoreNode.getHttpFullAddress());
                    break;
                }
            } catch (HttpStatusCodeException ex) {
                log.error("RestClientException in trustScore check to ts node: {} response: {} err: {}", trustScoreNode.getHttpFullAddress(), ex.getResponseBodyAsString(), ex.getMessage());
                trustScoreResponse = null;
            } catch (Exception ex) {
                log.error("Exception in trustScore check to ts node: {} err: {}", trustScoreNode.getHttpFullAddress(), ex.getMessage());
            }
        }

        if (trustScoreResponse == null) {
            log.error("No trustScore node could response to trustScore request for node {}", networkNodeData.getHttpFullAddress());
            return 0.0;
        }
        return ((GetUserTrustScoreResponse) trustScoreResponse.getBody()).getTrustScore();

    }

    @Override
    public void setTrustScores(List<NetworkNodeData> nodesList, List<NetworkNodeData> trustScoreNodeList) {
        NodeTrustScoreRequest nodeTrustScoreRequest = createNodeTrustScoreRequest(nodesList);
        List<NetworkNodeData> trustScoreNodesToHandle = chooseTrustScoreNodesFromList(trustScoreNodeList);
        NetworkNodeData firstTrustScoreNode = trustScoreNodesToHandle.get(0);
        NodeTrustScoreResponse nodeTrustScoreResponse = sendTrustScoreRequestToFirstTrustScoreNode(firstTrustScoreNode, nodeTrustScoreRequest);
        List<NetworkNodeData> trustScoreNodesForValidations = trustScoreNodeList.stream().skip(1).collect(Collectors.toList());
        if (nodeTrustScoreResponse.getNodeTrustScoreDataList() != null) {
            Map<Hash, NetworkNodeData> nodesToSet = createNodeMapFromList(nodesList);
            sendTrustScoreRequestForValidation(trustScoreNodesForValidations, nodeTrustScoreResponse);
            for (NodeTrustScoreData nodeTrustScoreData : nodeTrustScoreResponse.getNodeTrustScoreDataList()) {
                if (validateTrustScoreNodesConsensus(nodeTrustScoreData)) {
                    nodesToSet.get(nodeTrustScoreData.getNodeHash()).setTrustScore(nodeTrustScoreData.getTrustScore());
                } else {
                    log.error("The node {} didn't reach consensus", nodeTrustScoreData);
                    nodesToSet.get(nodeTrustScoreData.getNodeHash()).setTrustScore(-1.0);
                }
            }
        }
    }

    private Map<Hash, NetworkNodeData> createNodeMapFromList(List<NetworkNodeData> networkNodeData) {
        return networkNodeData.stream().collect(Collectors.toMap(NetworkNodeData::getHash, x -> x));
    }

    private NodeTrustScoreResponse sendTrustScoreRequestToFirstTrustScoreNode(NetworkNodeData firstTrustScoreNode, NodeTrustScoreRequest nodeTrustScoreRequest) {
        ResponseEntity<NodeTrustScoreResponse> trustScoreResponseEntity = null;
        try {
            trustScoreResponseEntity = httpRequest.postForEntity(
                    firstTrustScoreNode.getHttpFullAddress() + TRUSTSCORE_DATA_ENDPOINT, nodeTrustScoreRequest, NodeTrustScoreResponse.class);
            if (!HttpStatus.OK.equals(trustScoreResponseEntity.getStatusCode())) {
                log.error("Trust score node {} returned bad status code: {}", firstTrustScoreNode.getHttpFullAddress(), trustScoreResponseEntity);
                return new NodeTrustScoreResponse();
            }
            if (trustScoreResponseEntity.getBody() == null) {
                log.error("Trust score node {} returned null body: {}", firstTrustScoreNode.getHttpFullAddress(), trustScoreResponseEntity);
                return new NodeTrustScoreResponse();
            }
        } catch (Exception ex) {
            log.error("Error while contacting trustScoreNode {} exception", firstTrustScoreNode.getHttpFullAddress(), ex);
        }
        return trustScoreResponseEntity.getBody();
    }

    private void sendTrustScoreRequestForValidation(List<NetworkNodeData> trustScoreNodes, NodeTrustScoreResponse trustScoreResponseToAggregate) {
        trustScoreNodes.forEach(node -> sendTrustScoreResponseToAggregate(node, trustScoreResponseToAggregate));
    }

    private void sendTrustScoreResponseToAggregate(NetworkNodeData networkNodeData, NodeTrustScoreResponse trustScoreResponseToAggregate) {

        ResponseEntity<NodeTrustScoreResponse> trustScoreResponseEntity = null;
        try {
            trustScoreResponseEntity = httpRequest.postForEntity(
                    networkNodeData.getHttpFullAddress() + TRUSTSCORE_AGGREGATION_DATA_ENDPOINT, trustScoreResponseToAggregate, NodeTrustScoreResponse.class);
            if (!HttpStatus.OK.equals(trustScoreResponseEntity.getStatusCode())) {
                log.error("Trust score node {} returned bad status code: {}", networkNodeData.getHttpFullAddress(), trustScoreResponseEntity);
                return;
            }
            if (trustScoreResponseEntity.getBody() == null) {
                log.error("Trust score node {} returned null body: {}", networkNodeData.getHttpFullAddress(), trustScoreResponseEntity);
                return;
            }
        } catch (Exception ex) {
            log.error("Error while contacting trustScoreNode {} exception", networkNodeData.getHttpFullAddress(), ex);
        }
        trustScoreResponseToAggregate = trustScoreResponseEntity.getBody();
    }

    private boolean validateTrustScoreNodesConsensus(NodeTrustScoreData nodeTrustScoreResponse) {
        if (nodeTrustScoreResponse.getTrustScoreDataResults().size() != NUM_OF_TRUSTSCORE_NODES) {
            log.error("Not all of the trustScore transactions voted for the trust score  responses: {}", nodeTrustScoreResponse.getTrustScoreDataResults());
            return false;
        }
        int falseVote = 0;
        int trueVote = 0;

        for (NodeTrustScoreDataResult trustScoreData : nodeTrustScoreResponse.getTrustScoreDataResults()) {
            if (trustScoreData != null) {
                if (trustScoreData.isValid()) {
                    trueVote++;
                } else {
                    falseVote++;
                }
            }
        }
        return trueVote > falseVote;
    }

    private NodeTrustScoreRequest createNodeTrustScoreRequest(List<NetworkNodeData> nodesList) {
        NodeTrustScoreRequest nodeTrustScoreRequest = new NodeTrustScoreRequest();
        List<Hash> nodesToSend = nodesList.stream().map(NetworkNodeData::getHash).collect(Collectors.toList());
        nodeTrustScoreRequest.setNodesHash(nodesToSend);
        nodeTrustScoreRequest.setNodeManagerHash(NodeCryptoHelper.getNodeHash());
        trustScoreRequestCrypto.signMessage(nodeTrustScoreRequest);
        return nodeTrustScoreRequest;
    }

    private List<NetworkNodeData> chooseTrustScoreNodesFromList(List<NetworkNodeData> trustScoreNodeList) {
        Collections.shuffle(trustScoreNodeList);
        return trustScoreNodeList.stream().limit(NUM_OF_TRUSTSCORE_NODES).collect(Collectors.toList());
    }

}

