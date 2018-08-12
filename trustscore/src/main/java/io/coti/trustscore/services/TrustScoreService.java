package io.coti.trustscore.services;

import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.crypto.TransactionTrustScoreCrypto;
import io.coti.common.crypto.TrustScoreCrypto;
import io.coti.common.data.Hash;
import io.coti.common.data.TrustScoreData;
import io.coti.common.http.*;
import io.coti.common.data.TransactionTrustScoreData;
import io.coti.common.http.data.TransactionTrustScoreResponseData;
import io.coti.common.model.TrustScores;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;

import static io.coti.common.http.HttpStringConstants.*;

@Slf4j
@Service
public class TrustScoreService {
    @Autowired
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;
    @Autowired
    private TrustScoreCrypto trustScoreCrypto;
    @Autowired
    private TrustScores trustScores;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;

    public ResponseEntity<BaseResponse> getUserTrustScore(Hash userHash) {
        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }

        GetUserTrustScoreResponse getUserTrustScoreResponse = new GetUserTrustScoreResponse(userHash.toHexString(), trustScoreData.getTrustScore());
        return ResponseEntity.status(HttpStatus.OK).body(getUserTrustScoreResponse);
    }

    public ResponseEntity<BaseResponse> getTransactionTrustScore(Hash userHash, Hash transactionHash) {
        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }
        TransactionTrustScoreData transactionTrustScoreData = new TransactionTrustScoreData(userHash, transactionHash, trustScoreData.getTrustScore());
        transactionTrustScoreCrypto.signMessage(transactionTrustScoreData);
        TransactionTrustScoreResponseData transactionTrustScoreResponseData = new TransactionTrustScoreResponseData(transactionTrustScoreData);
        GetTransactionTrustScoreResponse getTransactionTrustScoreResponse = new GetTransactionTrustScoreResponse(transactionTrustScoreResponseData);
        return ResponseEntity.status(HttpStatus.OK).body(getTransactionTrustScoreResponse);
    }

    public ResponseEntity<BaseResponse> setKycTrustScore(SetKycTrustScoreRequest request) {
        try {
            TrustScoreData trustScoreData = new TrustScoreData(request.userHash, request.kycTrustScore, request.signature, new Hash(kycServerPublicKey));
            if (!trustScoreCrypto.verifySignature(trustScoreData)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new Response(KYC_TRUST_SCORE_AUTHENTICATION_ERROR, STATUS_ERROR));
            }
            TrustScoreData dbTrustScoreData = trustScores.getByHash(trustScoreData.getUserHash());
            Date date = new Date();
            if (dbTrustScoreData != null) {
                double updatedTrustScore = trustScoreData.getKycTrustScore() + (dbTrustScoreData.getTrustScore() - dbTrustScoreData.getKycTrustScore());
                trustScoreData.setTrustScore(updatedTrustScore);
                trustScoreData.setCreateTime(dbTrustScoreData.getCreateTime());
                trustScoreData.setLastUpdateTime(date);
            } else {
                trustScoreData.setTrustScore(trustScoreData.getKycTrustScore());
                trustScoreData.setCreateTime(date);
                trustScoreData.setLastUpdateTime(date);
            }
            trustScores.put(trustScoreData);
            SetKycTrustScoreResponse kycTrustScoreResponse = new SetKycTrustScoreResponse(trustScoreData);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(kycTrustScoreResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(KYC_TRUST_SET_ERROR, STATUS_ERROR));
        }
    }
}