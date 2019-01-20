package io.coti.trustscore.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionTrustScoreData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.TransactionTrustScoreResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.config.rules.InitialTrustScoreEventScore;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.crypto.TrustScoreCrypto;
import io.coti.trustscore.crypto.TrustScoreEventCrypto;
import io.coti.trustscore.crypto.TrustScoreUserTypeCrypto;
import io.coti.trustscore.data.Buckets.*;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.HighFrequencyEventScoreType;
import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.*;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.http.*;
import io.coti.trustscore.model.BucketEvents;
import io.coti.trustscore.model.TrustScores;
import io.coti.trustscore.model.UserTypeOfUsers;
import io.coti.trustscore.services.interfaces.IBucketEventService;
import io.coti.trustscore.utils.BucketBuilder;
import io.coti.trustscore.utils.DatesCalculation;
import io.coti.trustscore.utils.MathCalculation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.trustscore.http.HttpStringConstants.*;
import static io.coti.trustscore.utils.BucketBuilder.buildTransactionDataRequest;

@Slf4j
@Service
@Data
public class TrustScoreService {

    @Autowired
    private UserTypeOfUsers userTypeOfUsers;

    @Autowired
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;

    @Autowired
    private TrustScoreCrypto trustScoreCrypto;


    @Autowired
    private TrustScoreUserTypeCrypto trustScoreUserTypeCrypto;

    @Autowired
    private TrustScoreEventCrypto trustScoreEventCrypto;

    @Autowired
    private TrustScores trustScores;

    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;

    @Autowired
    private BucketTransactionService bucketTransactionService;

    @Autowired
    private BucketBehaviorEventsService bucketBehaviorEventsService;

    @Autowired
    private BucketInitialTrustScoreEventsService bucketInitialTrustScoreEventsService;

    @Autowired
    private BucketChargeBackEventsService bucketChargeBackEventsService;

    @Autowired
    private BucketNotFulfilmentEventsService bucketNotFulfilmentEventsService;

    @Autowired
    private BucketEvents bucketEvents;

    private List<IBucketEventService> bucketEventServiceList;

    private RulesData rulesData;

    @PostConstruct
    private void init() {
        log.info("{} is up", this.getClass().getSimpleName());
        bucketEventServiceList = new ArrayList<>();
        rulesData = loadRulesFromJsonFile();
        initBuckets();
        addBucketsToBucketEventServiceList();
    }

    public ResponseEntity<BaseResponse> addCentralServerEvent(InsertEventRequest request) {
        BaseResponse addingCentralEventResponse;
        try {
            BucketEventData bucketEventData = (BucketEventData) bucketEvents.getByHash(getBucketHashByUserHashAndEventType(request));
            if (bucketEventData.getEventDataHashToEventDataMap().get(request.uniqueIdentifier) != null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(CENTRAL_EVENT_EXIST, STATUS_ERROR));
            }

            addingCentralEventResponse = sendToSuitableService(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(addingCentralEventResponse);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(ADD_CENTRAL_EVENT_ERROR, STATUS_ERROR));
        }
    }

    public ResponseEntity<BaseResponse> setUserType(SetUserTypeRequest request) {
        try {
            log.info("Setting UserType: " + request.userHash + "=" + request.userType);

            TrustScoreData trustScoreData = trustScores.getByHash(request.userHash);
            if (trustScoreData == null) {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new Response(USER_HASH_IS_NOT_IN_DB, STATUS_ERROR));
            }

            if (!trustScoreUserTypeCrypto.verifySignature(request)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new Response(KYC_TRUST_SCORE_AUTHENTICATION_ERROR, STATUS_ERROR));
            }
            if (!changingIsLegal(trustScoreData)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new Response(CANT_CHANGE_FROM_NOT_CUSOMER_TYPE_MESSAGE, STATUS_ERROR));
            }
                UserType userType = UserType.enumFromString(request.userType);
                trustScoreData.setUserType(userType);
                trustScores.put(trustScoreData);
                userTypeOfUsers.put(new UserTypeOfUserData(request.userHash, userType));

                updateUserTypeInBuckets(request);

                SetUserTypeResponse setUserTypeResponse = new SetUserTypeResponse(userType, request.userHash);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(setUserTypeResponse);
            } catch(Exception e){
                log.error(e.getMessage());
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new Response(USER_TYPE_SET_ERROR, STATUS_ERROR));
            }

    }

    private boolean changingIsLegal(TrustScoreData trustScoreData) {
        return trustScoreData.getUserType() == UserType.CONSUMER;
    }

    public ResponseEntity<BaseResponse> getUserTrustScore(Hash userHash) {
        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }
        double currentTrustScore = calculateUserTrustScore(trustScoreData);
        GetUserTrustScoreResponse getUserTrustScoreResponse = new GetUserTrustScoreResponse(userHash.toHexString(), currentTrustScore);
        return ResponseEntity.status(HttpStatus.OK).body(getUserTrustScoreResponse);
    }



    public ResponseEntity<BaseResponse> getTransactionTrustScore(Hash userHash, Hash transactionHash, SignatureData signatureData) {

        try {
            PublicKey publicKey = CryptoHelper.getPublicKeyFromHexString(userHash.toHexString());

            if (!CryptoHelper.VerifyByPublicKey(transactionHash.getBytes(), signatureData.getR(), signatureData.getS(), publicKey)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST).body(new Response(BAD_SIGNATURE_ON_TRUST_SCORE_FOR_TRANSACTION));
            }

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Exception happened while trying to get public key user hash {}", e);

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST).body(new Response(BAD_SIGNATURE_ON_TRUST_SCORE_FOR_TRANSACTION));
        }

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

    public ResponseEntity<BaseResponse> setKycTrustScore(SetKycTrustScoreRequest request) {
        try {
            log.info("Setting KYC trust score: " + request.userHash + "=" + request.kycTrustScore);
            if (trustScores.getByHash(request.userHash) != null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(TRUST_SCORE_EXIST, STATUS_ERROR));
            }
            TrustScoreData trustScoreData = new TrustScoreData(request.userHash,
                    request.kycTrustScore,
                    request.signature,
                    new Hash(kycServerPublicKey),
                    UserType.enumFromString(request.userType));
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
                    .body(new Response(KYC_TRUST_SCORE_ERROR, STATUS_ERROR));
        }
    }

    public synchronized void addTransactionToTsCalculation(TransactionData transactionData) {
        TrustScoreData trustScoreData;

        try
        {
            trustScoreData = trustScores.getByHash(transactionData.getSenderHash());
        }
        catch (Exception e){
            return;
        }

        BucketEventData bucketEventData
                = (BucketEventData) bucketEvents.getByHash(getBucketHashByUserHashAndEventType(transactionData.getSenderHash(), EventType.TRANSACTION));
        if (bucketEventData.getEventDataHashToEventDataMap().get(transactionData.getHash()) != null) {
            return;
        }
        if (trustScoreData == null) {
            log.error("User not Exist");
        }

        if (transactionData.isZeroSpend() || transactionData.getDspConsensusResult() == null ||
                !transactionData.getDspConsensusResult().isDspConsensus()) {
            return;
        }

        LocalDate transactionConsensusDate = transactionData.getDspConsensusResult().getIndexingTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate currentDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        //TODO: case if transaction belong to the day before but only received now.

        if (currentDate.equals(transactionConsensusDate)) {
            addToTransactionBucketsCalculation(trustScoreData, transactionData);
        }

    }

    private void createBuckets(TrustScoreData trustScoreData) {
        try {
            for (EventType event : EventType.values()) {

                BucketEventData bucketEventData = BucketBuilder.createBucket(event, trustScoreData.getUserType(), trustScoreData.getUserHash());
                bucketEvents.put(bucketEventData);
                trustScoreData.getEventTypeToBucketHashMap().put(event, bucketEventData.getHash());
            }
            trustScores.put(trustScoreData);
        } catch (IllegalAccessException | InstantiationException e) {
            log.error(e.toString());
        }
    }

    private void initBuckets() {
        BucketTransactionService.init(rulesData);
        bucketBehaviorEventsService.init(rulesData);
        bucketInitialTrustScoreEventsService.init(rulesData);
        bucketChargeBackEventsService.init(rulesData);
    }


    private void addBucketsToBucketEventServiceList() {
        bucketEventServiceList.add(bucketBehaviorEventsService);
        bucketEventServiceList.add(bucketTransactionService);
        bucketEventServiceList.add(bucketInitialTrustScoreEventsService);
        bucketEventServiceList.add(bucketChargeBackEventsService);
    }

    private void addToTransactionBucketsCalculation(TrustScoreData trustScoreData, TransactionData transactionData) {
        TransactionEventData transactionEventData
                = new TransactionEventData(buildTransactionDataRequest(trustScoreData.getUserHash(), trustScoreData.getSignature(), transactionData));
        BucketTransactionEventsData bucketTransactionEventsData
                = (BucketTransactionEventsData) bucketEvents.getByHash(trustScoreData.getEventTypeToBucketHashMap().get(transactionEventData.getEventType()));
        bucketTransactionService.addEventToCalculations(transactionEventData, bucketTransactionEventsData);

        bucketEvents.put(bucketTransactionEventsData);

        if (transactionData.getAmount().doubleValue() > 0) {
            addTransactionToChargeBackBucket(transactionData.getSenderHash(), transactionData);
        }
    }

    private void addTransactionToChargeBackBucket(Hash userHash, TransactionData transactionData) {
        Hash bucketHash = new Hash(ByteBuffer.allocate(userHash.getBytes().length + Integer.BYTES).
                put(userHash.getBytes()).putInt(EventType.HIGH_FREQUENCY_EVENTS.getValue()).array());
        BucketChargeBackEventsData bucketChargeBackEventsData = (BucketChargeBackEventsData) bucketEvents.getByHash(bucketHash);
        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData, bucketChargeBackEventsData);
        bucketEvents.put(bucketChargeBackEventsData);
    }

    public double calculateUserTrustScore(TrustScoreData trustScoreData) {
        double eventsTrustScore = 0;

        for (IBucketEventService bucketEventService : bucketEventServiceList) {
            BucketEventData bucketEventData =
                    (BucketEventData) bucketEvents.getByHash(trustScoreData.getEventTypeToBucketHashMap().get(bucketEventService.getBucketEventType()));
            if (bucketEventData != null) {
                eventsTrustScore += bucketEventService.getBucketSumScore(bucketEventData);
            }
            bucketEventData.setLastUpdate(DatesCalculation.setDateOnBeginningOfDay(new Date()));
        }
        return Math.max(eventsTrustScore + getKycScore(trustScoreData), 0.1);
    }

    private double getKycScore(TrustScoreData trustScoreData) {

        InitialTrustScoreEventScore kycInitialTrustScoreEventScore
                = BucketTransactionService.getRulesData().getUsersRules(trustScoreData.getUserType())
                .getInitialTrustScore().getComponentByType(InitialTrustScoreType.KYC);
        Date beginningOfToday = DatesCalculation.setDateOnBeginningOfDay(new Date());
        Date beginningDayOfCreateDate = DatesCalculation.setDateOnBeginningOfDay(trustScoreData.getCreateTime());
        int daysDifference = DatesCalculation.calculateDaysDiffBetweenDates(beginningOfToday, beginningDayOfCreateDate);

        return MathCalculation.evaluateExpression(kycInitialTrustScoreEventScore.getDecay().replace("T", String.valueOf(daysDifference)))
                * trustScoreData.getKycTrustScore() * kycInitialTrustScoreEventScore.getWeight();
    }

    private void updateUserTypeInBuckets(SetUserTypeRequest request) {
        TrustScoreData trustScoreData = trustScores.getByHash(request.userHash);
        for (Map.Entry<EventType, Hash> eventTypeToBucketHashEntry
                : trustScoreData.getEventTypeToBucketHashMap().entrySet()) {
            Hash bucketHash = eventTypeToBucketHashEntry.getValue();
            BucketEventData bucket = (BucketEventData) bucketEvents.getByHash(bucketHash);
            if (bucket != null) {
                bucket.setUserType(UserType.enumFromString(request.userType));
                bucketEvents.put(bucket);
            }
        }
    }

    private RulesData loadRulesFromJsonFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream jsonConfigStream = new ClassPathResource("trustScoreRules.json").getInputStream();
            rulesData = objectMapper.readValue(jsonConfigStream, RulesData.class);
            log.info(rulesData.toString());
        } catch (IOException e) {
            log.error("Error reading from JSON file", e);
            log.error("Shutting down!");
            System.exit(1);
        }
        return rulesData;
    }

    private BaseResponse sendToSuitableService(InsertEventRequest request) {
        switch (request.eventType) {
            case INITIAL_EVENT: {
                return sendToBucketInitialTrustScoreEventsService(request);
            }
            case HIGH_FREQUENCY_EVENTS: {
                return sendToHighFrequencyEventScoreService(request);
            }
            case BEHAVIOR_EVENT: {
                return sendToBucketBehaviorEventsService(request);
            }
            case NOT_FULFILMENT_EVENT: {
                return sendToBucketNotFulfilmentEventsService(request);
            }
            default:
                return null;
            // TODO : define response
        }
    }

    private BaseResponse sendToBucketInitialTrustScoreEventsService(InsertEventRequest request) {

        InitialTrustScoreEventsData initialTrustScoreEventsData =
                new InitialTrustScoreEventsData(request);

        Hash bucketHash = getBucketHashByUserHashAndEventType(request);
        BucketInitialTrustScoreEventsData bucketInitialTrustScoreEventsData =
                (BucketInitialTrustScoreEventsData) bucketEvents.getByHash(bucketHash);

        bucketInitialTrustScoreEventsService.addEventToCalculations(
                initialTrustScoreEventsData,
                bucketInitialTrustScoreEventsData);

        bucketEvents.put(bucketInitialTrustScoreEventsData);
        return new SetInitialTrustScoreEventResponse(request.userHash, request.eventType, request.getInitialTrustScoreType(), request.getScore());
    }

    private BaseResponse sendToHighFrequencyEventScoreService(InsertEventRequest request) {
        if (request.getHighFrequencyEventScoreType() == HighFrequencyEventScoreType.CHARGE_BACK) {
            ChargeBackEventsData chargeBackEventsData = new ChargeBackEventsData(request);

            Hash bucketHash = getBucketHashByUserHashAndEventType(request);
            BucketChargeBackEventsData bucketChargeBackEventsData = (BucketChargeBackEventsData) bucketEvents.getByHash(bucketHash);

            bucketChargeBackEventsService.addEventToCalculations(chargeBackEventsData, bucketChargeBackEventsData);
            bucketEvents.put(bucketChargeBackEventsData);

            Hash transactionDataHash = (request.getTransactionData() != null) ? request.getTransactionData().getHash() : null;
            return new SetHighFrequencyEventScoreResponse(request.userHash, request.eventType, request.getHighFrequencyEventScoreType(), transactionDataHash);
        }

        // TODO: other high frequency events
        return null;
    }

    private BaseResponse sendToBucketNotFulfilmentEventsService(InsertEventRequest request) {
        NotFulfilmentEventsData notFulfilmentEventsData = new NotFulfilmentEventsData(request);

        Hash bucketHash = getBucketHashByUserHashAndEventType(request);
        BucketNotFulfilmentEventsData bucketNotFulfilmentEventsData = (BucketNotFulfilmentEventsData) bucketEvents.getByHash(bucketHash);

        bucketNotFulfilmentEventsService.addEventToCalculations(notFulfilmentEventsData, bucketNotFulfilmentEventsData);

        try {
            bucketEvents.put(bucketNotFulfilmentEventsData);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return new SetNotFulfilmentEventScoreResponse(request.userHash, request.eventType, request.getCompensableEventScoreType());
    }

    private BaseResponse sendToBucketBehaviorEventsService(InsertEventRequest request) {
        BehaviorEventsData behaviorEventsData = new BehaviorEventsData(request);

        Hash bucketHash = getBucketHashByUserHashAndEventType(request);
        BucketBehaviorEventsData bucketBehaviorEventsData = (BucketBehaviorEventsData) bucketEvents.getByHash(bucketHash);

        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData, bucketBehaviorEventsData);
        try {
            bucketEvents.put(bucketBehaviorEventsData);
        } catch (Exception e) {
            log.error(e.toString());
        }

        Hash transactionDataHash = (request.getTransactionData() != null) ? request.getTransactionData().getHash() : null;
        return new SetBehaviorEventResponse(request.userHash, request.eventType, request.getBehaviorEventsScoreType(), transactionDataHash);
    }



    private Hash getBucketHashByUserHashAndEventType(InsertEventRequest request) {
        return getBucketHashByUserHashAndEventType(request.userHash, request.eventType);
    }

    private Hash getBucketHashByUserHashAndEventType(Hash userHash, EventType eventType) {
        return new Hash(ByteBuffer.allocate(userHash.getBytes().length + Integer.BYTES).
                put(userHash.getBytes()).putInt(eventType.getValue()).array());
    }
}