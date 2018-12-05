package io.coti.nodemanager;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeTrustScoreData;
import io.coti.basenode.data.NodeTrustScoreDataResult;
import io.coti.basenode.http.data.NodeTrustScoreRequest;
import io.coti.basenode.http.data.NodeTrustScoreResponse;
import io.coti.nodemanager.crypto.NodeTrustScoreRequestCrypto;
import io.coti.nodemanager.services.TrustScoreService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.when;


@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class TrustScoreServiceTest {

    @MockBean
    private RestTemplate restTemplate;

    private Hash nodeToSendHash = new Hash("1");

    private static final double TRUSTSCORE_TO_TEST = 55.5;

    @Autowired
    private NodeTrustScoreRequestCrypto trustScoreRequestCrypto;
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSetTrustScoresPositive(){
        List<NetworkNodeData> trustScoreNodes = createTrustScoreNodes();
        // end of trustScore nodes set
        List<NetworkNodeData> nodesToSend = createNodesToSend();
        NodeTrustScoreData nodeTrustScoreData = createTrustScoreData();
        NodeTrustScoreDataResult nodeTrustScore1DataResult = createTrustScoreDataResult(true);
        List<NodeTrustScoreDataResult> nodeTrustScoreDataResults = new LinkedList<>();
        Collections.addAll(nodeTrustScoreDataResults, nodeTrustScore1DataResult, nodeTrustScore1DataResult, nodeTrustScore1DataResult);
        nodeTrustScoreData.setTrustScoreDataResults(nodeTrustScoreDataResults);
        List<NodeTrustScoreData> trustScoreDataList = new LinkedList<>();
        trustScoreDataList.add(nodeTrustScoreData);
        ResponseEntity<NodeTrustScoreResponse> mockedResponse = createMockedNodeTrustScoreResponse(trustScoreDataList);
        when(restTemplate.postForEntity( anyString(), any(),eq(NodeTrustScoreResponse.class))).thenReturn(mockedResponse);
        TrustScoreService trustScoreService = new TrustScoreService(restTemplate, trustScoreRequestCrypto);
        trustScoreService.setTrustScores(nodesToSend,trustScoreNodes);
        Assert.assertTrue(nodesToSend.get(0).getTrustScore().equals(TRUSTSCORE_TO_TEST));
    }

    @Test
    public void testSetTrustScoresNegativeVoting(){
        List<NetworkNodeData> trustScoreNodes = createTrustScoreNodes();
        // end of trustScore nodes set
        List<NetworkNodeData> nodesToSend = createNodesToSend();
        NodeTrustScoreData nodeTrustScoreData = createTrustScoreData();
        NodeTrustScoreDataResult positiveNodeTrustScore1DataResult = createTrustScoreDataResult(true);
        NodeTrustScoreDataResult negativeNodeTrustScore1DataResult = createTrustScoreDataResult(false);
        List<NodeTrustScoreDataResult> nodeTrustScoreDataResults = new LinkedList<>();
        Collections.addAll(nodeTrustScoreDataResults, positiveNodeTrustScore1DataResult, negativeNodeTrustScore1DataResult, negativeNodeTrustScore1DataResult);
        nodeTrustScoreData.setTrustScoreDataResults(nodeTrustScoreDataResults);
        List<NodeTrustScoreData> trustScoreDataList = new LinkedList<>();
        trustScoreDataList.add(nodeTrustScoreData);
        ResponseEntity<NodeTrustScoreResponse> mockedResponse = createMockedNodeTrustScoreResponse(trustScoreDataList);
        when(restTemplate.postForEntity( anyString(), any(),eq(NodeTrustScoreResponse.class))).thenReturn(mockedResponse);
        TrustScoreService trustScoreService = new TrustScoreService(restTemplate, trustScoreRequestCrypto);
        trustScoreService.setTrustScores(nodesToSend,trustScoreNodes);
        Assert.assertTrue(nodesToSend.get(0).getTrustScore().equals(-1.0));
    }

    private ResponseEntity<NodeTrustScoreResponse> createMockedNodeTrustScoreResponse(List<NodeTrustScoreData> trustScoreDataList) {
        NodeTrustScoreResponse nodeTrustScoreResponse = new NodeTrustScoreResponse();
        nodeTrustScoreResponse.setNodeTrustScoreDataList(trustScoreDataList);
        return ResponseEntity.ok(nodeTrustScoreResponse);
    }

    private NodeTrustScoreData createTrustScoreData() {
        NodeTrustScoreData nodeTrustScoreData = new NodeTrustScoreData();
        nodeTrustScoreData.setNodeHash(nodeToSendHash);
        nodeTrustScoreData.setTrustScore(TRUSTSCORE_TO_TEST);
        return nodeTrustScoreData;
    }

    private NodeTrustScoreDataResult createTrustScoreDataResult(boolean valid) {
        NodeTrustScoreDataResult nodeTrustScore1DataResult = new NodeTrustScoreDataResult();
        nodeTrustScore1DataResult.setValid(valid);
        return nodeTrustScore1DataResult;
    }

    private List<NetworkNodeData> createTrustScoreNodes() {
        NetworkNodeData trustScore1 = new NetworkNodeData();
        trustScore1.setHttpPort("1");
        trustScore1.setAddress("1");
        Assert.assertTrue(trustScore1.getHttpFullAddress().equals("http://1:1"));
        NetworkNodeData trustScore2 = new NetworkNodeData();
        trustScore2.setHttpPort("2");
        trustScore2.setAddress("2");
        Assert.assertTrue(trustScore2.getHttpFullAddress().equals("http://2:2"));
        NetworkNodeData trustScore3 = new NetworkNodeData();
        trustScore3.setHttpPort("3");
        trustScore3.setAddress("3");
        Assert.assertTrue(trustScore3.getHttpFullAddress().equals("http://3:3"));
        List<NetworkNodeData> nodeDataList = new LinkedList<>();
        Collections.addAll(nodeDataList,trustScore1, trustScore2, trustScore3);
        return nodeDataList;
    }

    private List<NetworkNodeData> createNodesToSend() {
        NetworkNodeData nodeToSend = new NetworkNodeData();
        nodeToSend.setHash(nodeToSendHash);
        List<NetworkNodeData> nodesToSend = new LinkedList<>();
        nodesToSend.add(nodeToSend);
        return nodesToSend;
    }


    private NodeTrustScoreRequest mockTrustScoreRequest(List<NetworkNodeData> nodesList){
        NodeTrustScoreRequest nodeTrustScoreRequest = new NodeTrustScoreRequest();
        List<Hash> nodesToSendHashes = nodesList.stream().map(NetworkNodeData::getHash).collect(Collectors.toList());
        nodeTrustScoreRequest.setNodesHash(nodesToSendHashes);
        return nodeTrustScoreRequest;
    }


}
