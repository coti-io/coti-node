package io.coti.nodemanager.services;

import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.GetTrustScoreRequest;
import io.coti.basenode.http.GetUserTrustScoreResponse;
import io.coti.nodemanager.services.interfaces.ITrustScoreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class TrustScoreService implements ITrustScoreService {

    private static final String GET_TRUSTSCORE_ENDPOINT = "/usertrustscore";


    @Autowired
    private NodeManagementService nodeManagementService;

    @Override
    public Double getTrustScore(NetworkNodeData networkNodeData) {
        GetTrustScoreRequest getTrustScoreRequest = new GetTrustScoreRequest();
        getTrustScoreRequest.userHash = networkNodeData.getNodeHash();
        RestTemplate getRequest = new RestTemplate();
        ResponseEntity<Object> trustScoreResponse = null;
        for(NetworkNodeData trustScoreNode : nodeManagementService.getAllNetworkData().getTrustScoreNetworkNodesList()){
            try {
                trustScoreResponse = getRequest.postForEntity
                        (trustScoreNode.getHttpFullAddress() + GET_TRUSTSCORE_ENDPOINT, getTrustScoreRequest, Object.class);

                if (!HttpStatus.OK.equals(trustScoreResponse.getStatusCode())) {
                    log.error("Trust score node {} returned : {}", trustScoreNode.getHttpFullAddress(), trustScoreResponse);
                    trustScoreResponse = null;
                }
                if(trustScoreResponse == null || trustScoreResponse.getBody() == null){
                    log.error("Trust score node {} returned null", trustScoreNode.getHttpFullAddress());
                    trustScoreResponse = null;
                }
                else{
                    log.info("Response from node: {} . TrustScoreNode: {}", trustScoreResponse, trustScoreNode.getHttpFullAddress());
                    break;
                }
            }
            catch (HttpStatusCodeException  ex){
                log.error("RestClientException in trustScore check to ts node: {} response: {} err: {}", trustScoreNode.getHttpFullAddress(), ex.getResponseBodyAsString(), ex.getMessage());
                trustScoreResponse = null;
            }
            catch (Exception ex){
                log.error("Exception in trustScore check to ts node: {} err: {}", trustScoreNode.getHttpFullAddress(), ex.getMessage());
            }
        }

        if(trustScoreResponse == null){
            log.error("No trustScore node could response to trustScore request for node {}", networkNodeData.getHttpFullAddress());
            return 0.0;
        }
        return ((GetUserTrustScoreResponse)trustScoreResponse.getBody()).getTrustScore();


    }

    private NetworkNodeData chooseTrustScoreNode() { // ATM not in use
        Random rand = new Random();
        List<NetworkNodeData> nodeDataList = nodeManagementService.getAllNetworkData().getTrustScoreNetworkNodesList();
        return nodeDataList.get(rand.nextInt(nodeDataList.size()));
    }
}

