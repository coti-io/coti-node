package io.coti.trustscore.services;

import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionTrustScoreData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.TransactionTrustScoreResponseData;
import io.coti.trustscore.crypto.TrustScoreCrypto;
import io.coti.trustscore.crypto.TrustScoreEventCrypto;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.CentralEventData;
import io.coti.trustscore.data.Events.TransactionEventData;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.http.*;
import io.coti.trustscore.model.BucketTransactionEvents;
import io.coti.trustscore.model.TransactionEvents;
import io.coti.trustscore.model.TrustScores;
import io.coti.trustscore.rulesData.Component;
import io.coti.trustscore.rulesData.InitialTrustType;
import io.coti.trustscore.rulesData.RulesData;
import io.coti.trustscore.utils.DatesCalculation;
import io.coti.trustscore.utils.MathCalculation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Vector;

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

    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;

    @Autowired
    private BucketTransactionService bucketTransactionService;


    @Autowired
    private TransactionEvents transactionEvents;

    @Autowired
    private BucketTransactionEvents bucketTransactionEvents;

    private List<BucketEventService> bucketEventServiceList;

    @PostConstruct
    private void init() {
        log.info("{} is up", this.getClass().getSimpleName());
        bucketEventServiceList = new Vector<>();
        bucketEventServiceList.add(bucketTransactionService);
        RulesData rulesData = loadRulesFromFile();
        bucketTransactionService.init(rulesData);
    }

    private RulesData loadRulesFromFile() {
        RulesData rulesData = null;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource("trustScoreRules.xml").getFile());
            JAXBContext jaxbContext = JAXBContext.newInstance(RulesData.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            rulesData = (RulesData) jaxbUnmarshaller.unmarshal(file);
        } catch (JAXBException e) {
            log.error("Error reading from XML file", e);
            log.error("Shutting down!");
            System.exit(1);
        }
        return rulesData;
    }


    public ResponseEntity<BaseResponse> addCentralServerEvent(InsertTrustScoreEventRequest request) {

        CentralEventData centralEventData = request.convertToCentralEvent();
        if (!trustScoreEventCrypto.verifySignature(centralEventData)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(TRUST_SCORE_EVENT_AUTHENTICATION_ERROR, STATUS_ERROR));
        }


        //TODO: here we need to add event to the appropiate bucket

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public void addTransactionToTsCalculation(TransactionData transactionData) {
        if (transactionData.isZeroSpend() || transactionData.getDspConsensusResult() == null ||
                !transactionData.getDspConsensusResult().isDspConsensus()) return;

        LocalDate transactionConsensusDate = transactionData.getDspConsensusResult().getIndexingTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();


        TrustScoreData trustScoreData = trustScores.getByHash(transactionData.getSenderHash());

        //TODO: case if transaction belong to the day before but only received now.

        if (currentDate.equals(transactionConsensusDate)) {

            if (transactionEvents.getByHash(transactionData.getHash()) != null)
                return;

            TransactionEventData transactionEventData = new TransactionEventData(transactionData);
            trustScoreData.addEvent(transactionEventData);

            transactionEvents.put(transactionData);
            trustScores.put(trustScoreData);

            BucketTransactionEventsData bucketTransactionEventsData = bucketTransactionService.addEventToCalculations(transactionEventData,
                    (BucketTransactionEventsData) trustScoreData.getLastBucketEventData().get(transactionEventData.getEventType()));
            bucketTransactionEvents.put(bucketTransactionEventsData);
        }
    }

    public ResponseEntity<BaseResponse> getUserTrustScore(Hash userHash) {
        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }



        double currentTrustScore = calculateUserTrustScore(trustScores.getByHash(userHash));



        GetUserTrustScoreResponse getUserTrustScoreResponse = new GetUserTrustScoreResponse(userHash.toHexString(), currentTrustScore);
        return ResponseEntity.status(HttpStatus.OK).body(getUserTrustScoreResponse);
    }

    public ResponseEntity<BaseResponse> getTransactionTrustScore(Hash userHash, Hash transactionHash) {
        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }

        double currentTransactionsTrustScore = calculateUserTrustScore(trustScoreData);
        TransactionTrustScoreData transactionTrustScoreData = new TransactionTrustScoreData(userHash, transactionHash, currentTransactionsTrustScore);
        transactionTrustScoreCrypto.signMessage(transactionTrustScoreData);
        TransactionTrustScoreResponseData transactionTrustScoreResponseData = new TransactionTrustScoreResponseData(transactionTrustScoreData);
        GetTransactionTrustScoreResponse getTransactionTrustScoreResponse = new GetTransactionTrustScoreResponse(transactionTrustScoreResponseData);
        return ResponseEntity.status(HttpStatus.OK).body(getTransactionTrustScoreResponse);
    }

    private double calculateUserTrustScore(TrustScoreData trustScoreData){
        double currentTrustScore = 0;
        for (BucketEventService bucketEventService : bucketEventServiceList) {
            BucketEventData bucketEventData = trustScoreData.getLastBucketEventData().get(bucketEventService.getBucketEventType());
            currentTrustScore += bucketEventService.getBucketSumScore(bucketEventData);
        }

        Component kycComponent =  bucketTransactionService.getRulesData().getUsersRules(trustScoreData.getUserType()).getInitialTrustScore().getComponentByType(InitialTrustType.KYC);
        int daysDifference =  DatesCalculation.calculateDaysDiffBetweenDates(new Date(),trustScoreData.getCreateTime());
        currentTrustScore = currentTrustScore + Math.exp(-MathCalculation.evaluteExpression(kycComponent.getDecay())*daysDifference ) * trustScoreData.getKycTrustScore() * kycComponent.getWeight();
        return currentTrustScore;
    }

    public ResponseEntity<BaseResponse> setKycTrustScore(SetKycTrustScoreRequest request) {
        try {
            log.info("Setting KYC trust score: " + request.userHash + "=" + request.kycTrustScore);
            TrustScoreData trustScoreData = new TrustScoreData(request.userHash, request.kycTrustScore, request.signature, new Hash(kycServerPublicKey), UserType.WALLET);
            if (!trustScoreCrypto.verifySignature(trustScoreData)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new Response(KYC_TRUST_SCORE_AUTHENTICATION_ERROR, STATUS_ERROR));
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