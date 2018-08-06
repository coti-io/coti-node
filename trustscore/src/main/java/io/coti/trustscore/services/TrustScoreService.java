package io.coti.trustscore.services;

import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.Hash;
import io.coti.common.data.TrustScoreData;
import io.coti.common.http.GetTransactionTrustScoreResponse;
import io.coti.common.http.GetUserTrustScoreResponse;
import io.coti.common.http.Response;
import io.coti.common.http.SetTrustScoreRequest;
import io.coti.common.http.data.TrustScoreResponseData;
import io.coti.common.model.TrustScores;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static io.coti.common.http.HttpStringConstants.NON_EXISTING_USER_MESSAGE;
import static io.coti.common.http.HttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class TrustScoreService {

    @Autowired
    private TrustScores trustScores;

    public ResponseEntity<Response> getUserTrustScore(Hash userHash) {
        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }

        GetUserTrustScoreResponse getUserTrustScoreResponse = new GetUserTrustScoreResponse(userHash.toHexString(), trustScoreData.getTrustScore());
        return ResponseEntity.ok(getUserTrustScoreResponse);
    }

    public ResponseEntity<Response> getTransactionTrustScore(Hash userHash, Hash transactionHash) {
        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }
        GetTransactionTrustScoreResponse getTransactionTrustScoreResponse =
                new GetTransactionTrustScoreResponse(
                        new TrustScoreResponseData(
                                userHash,
                                transactionHash,
                                trustScoreData.getTrustScore(),
                                NodeCryptoHelper.getNodeHash(),
                                NodeCryptoHelper.signMessage((transactionHash.toHexString() + trustScoreData.getTrustScore().toString()).getBytes())
                        ));
        return ResponseEntity.ok(getTransactionTrustScoreResponse);
    }

    public void setUserTrustScore(SetTrustScoreRequest request) {
        trustScores.put(new TrustScoreData(request.userHash, request.trustScore));
        log.info("New trust score received: " + request.userHash + "=" + request.trustScore);
    }
}