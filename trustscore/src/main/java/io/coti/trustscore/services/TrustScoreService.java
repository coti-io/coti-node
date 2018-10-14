package io.coti.trustscore.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionTrustScoreData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.TransactionTrustScoreResponseData;
import io.coti.trustscore.crypto.TrustScoreCrypto;
import io.coti.trustscore.crypto.TrustScoreEventCrypto;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Events.CentralEventData;
import io.coti.trustscore.data.Events.EventData;
import io.coti.trustscore.data.Events.TransactionEventData;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.http.*;
import io.coti.trustscore.model.TransactionEvents;
import io.coti.trustscore.model.TrustScores;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static io.coti.basenode.http.HttpStringConstants.*;

@Slf4j
@Service
public class TrustScoreService {
    @Autowired
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;

    @Autowired
    private TrustScoreCrypto trustScoreCrypto;

    @Autowired
    private TrustScoreEventCrypto trustScoreEventCrypto;
    @Autowired
    private TrustScores trustScores;
    @Autowired
    private TrustScoreRulesInitService trustScoreRulesInitService;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;

    @Autowired
    private BucketTransactionService bucketTransactionService;

    @Value("#{'${trustscore.server.addresses}'.split(',')}")
    private List<String> trustscoreServerAddresses;

    @Autowired
    private ISender sender;

//    @Autowired
//    private TrustScores trustScoresUsers;
    @Autowired
    private TransactionEvents transactionEvents;

    @PostConstruct
    private void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

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

    public void sendEventToTrustScoreNodes(EventData eventData) {
        trustscoreServerAddresses.forEach(address -> sender.send(eventData, address));
    }

    public ResponseEntity<BaseResponse> addCentralServerEvent(InsertTrustScoreEventRequest request) {

        CentralEventData centralEventData = request.convertToCentralEvent(new Hash(kycServerPublicKey));
        if (!trustScoreEventCrypto.verifySignature(centralEventData)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(TRUST_SCORE_EVENT_AUTHENTICATION_ERROR, STATUS_ERROR));
        }


        //TODO: here we need to add event to the appropiate bucket

        sendEventToTrustScoreNodes(centralEventData);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public void addTransactionToTsCalculation(TransactionData transactionData) {
        if (transactionData.isZeroSpend() || transactionData.getDspConsensusResult() == null ||
                !transactionData.getDspConsensusResult().isDspConsensus()) return;

        LocalDate transactionConsensusDate = transactionData.getDspConsensusResult().getIndexingTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();


        TrustScoreData trustScoreData = trustScores.getByHash(transactionData.getSenderHash());

//        if (trustScoreData == null)
//            trustScoreData = new TrustScoreData(transactionData.getSenderHash(), UserType.WALLET);
//        //TODO: case if transaction belong to the day before but only received now.

        if (currentDate.equals(transactionConsensusDate)) {

            if (transactionEvents.getByHash(transactionData.getHash()) != null)
                return;

            TransactionEventData transactionEventData = new TransactionEventData(transactionData);
            trustScoreData.addEvent(transactionEventData);
            transactionEvents.put(transactionData);
            //trustScores.put(trustScoreData);

            bucketTransactionService.addEventToCalculations(transactionEventData,
                    (BucketTransactionEventsData) trustScoreData.getLastBucketEventData().get(transactionEventData.getEventType()));
        }
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
            log.info("Setting KYC trust score: " + request.userHash + "=" + request.kycTrustScore);
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