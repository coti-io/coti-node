package io.coti.trustscore.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.http.GetUserTrustScoreResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.TransactionTrustScoreResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.crypto.GetTransactionTrustScoreRequestCrypto;
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
import io.coti.trustscore.services.interfaces.IBucketEventService;
import io.coti.trustscore.utils.BucketBuilder;
import io.coti.trustscore.utils.DatesCalculation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.trustscore.http.HttpStringConstants.*;
import static io.coti.trustscore.utils.BucketBuilder.buildTransactionDataRequest;

@Slf4j
@Service
@Data
public class TrustScoreService {

    @Autowired
    private ExpandedTransactionTrustScoreCrypto expandedTransactionTrustScoreCrypto;
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
    @Autowired
    private GetTransactionTrustScoreRequestCrypto getTransactionTrustScoreRequestCrypto;
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

    public ResponseEntity<IResponse> addKycServerEvent(InsertEventRequest request) {
        IResponse addingKycServerEventResponse;
        try {
            if (!request.getSignerHash().equals(new Hash(kycServerPublicKey))) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(INVALID_SIGNER, STATUS_ERROR));
            }

            BucketEventData bucketEventData = (BucketEventData) bucketEvents.getByHash(getBucketHashByUserHashAndEventType(request));
            if (bucketEventData.getEventDataHashToEventDataMap().get(request.getUniqueIdentifier()) != null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(KYC_SERVER_EVENT_EXIST, STATUS_ERROR));
            }

            addingKycServerEventResponse = sendToSuitableService(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(addingKycServerEventResponse);

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(ADD_KYC_SERVER_EVENT_ERROR, STATUS_ERROR));
        }
    }

    public ResponseEntity<IResponse> setUserType(SetUserTypeRequest request) {
        try {
            log.info("Setting UserType: " + request.getUserHash() + "=" + request.getUserType());

            TrustScoreData trustScoreData = trustScores.getByHash(request.getUserHash());
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
                        .body(new Response(USER_TYPE_ALREADY_UPDATED, STATUS_ERROR));
            }
            UserType userType = UserType.enumFromString(request.getUserType());
            trustScoreData.setUserType(userType);
            trustScores.put(trustScoreData);

            updateUserTypeInBuckets(trustScoreData);

            SetUserTypeResponse setUserTypeResponse = new SetUserTypeResponse(userType, request.getUserHash());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(setUserTypeResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(USER_TYPE_SET_ERROR, STATUS_ERROR));
        }

    }

    private boolean changingIsLegal(TrustScoreData trustScoreData) {
        return trustScoreData.getUserType().equals(UserType.CONSUMER);
    }

    public ResponseEntity<IResponse> getUserTrustScore(Hash userHash) {
        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }
        double currentTrustScore = calculateUserTrustScore(trustScoreData);
        GetUserTrustScoreResponse getUserTrustScoreResponse = new GetUserTrustScoreResponse(userHash.toString(), currentTrustScore, trustScoreData.getUserType().toString());
        return ResponseEntity.status(HttpStatus.OK).body(getUserTrustScoreResponse);
    }


    public ResponseEntity<IResponse> getTransactionTrustScore(GetTransactionTrustScoreRequest getTransactionTrustScoreRequest) {

        if (!getTransactionTrustScoreRequestCrypto.verifySignature(getTransactionTrustScoreRequest)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST).body(new Response(BAD_SIGNATURE_ON_TRUST_SCORE_FOR_TRANSACTION));
        }

        Hash userHash = getTransactionTrustScoreRequest.getUserHash();
        Hash transactionHash = getTransactionTrustScoreRequest.getTransactionHash();

        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }

        double currentTransactionsTrustScore = calculateUserTrustScore(trustScoreData);
        ExpandedTransactionTrustScoreData expandedTransactionTrustScoreData = new ExpandedTransactionTrustScoreData(userHash, transactionHash, currentTransactionsTrustScore);
        expandedTransactionTrustScoreCrypto.signMessage(expandedTransactionTrustScoreData);
        TransactionTrustScoreData transactionTrustScoreData = new TransactionTrustScoreData(expandedTransactionTrustScoreData);
        TransactionTrustScoreResponseData transactionTrustScoreResponseData = new TransactionTrustScoreResponseData(transactionTrustScoreData);
        GetTransactionTrustScoreResponse getTransactionTrustScoreResponse = new GetTransactionTrustScoreResponse(transactionTrustScoreResponseData);
        return ResponseEntity.status(HttpStatus.OK).body(getTransactionTrustScoreResponse);
    }

    public ResponseEntity<IResponse> getUserTrustScoreComponents(Hash userHash) {
        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }

        List<BucketEventData> bucketEventDataList = new ArrayList<>();
        for (IBucketEventService bucketEventService : bucketEventServiceList) {
            BucketEventData bucketEventData =
                    (BucketEventData) bucketEvents.getByHash(trustScoreData.getEventTypeToBucketHashMap().get(bucketEventService.getBucketEventType()));
            bucketEventDataList.add(bucketEventData);
        }

        GetUserTrustScoreComponentsResponse getUserTrustScoreComponentsResponse = new GetUserTrustScoreComponentsResponse(trustScoreData, bucketEventDataList);
        return ResponseEntity.status(HttpStatus.OK).body(getUserTrustScoreComponentsResponse);
    }

    public ResponseEntity<IResponse> setKycTrustScore(SetKycTrustScoreRequest request) {
        SetKycTrustScoreResponse kycTrustScoreResponse;

        try {
            log.info("Setting KYC trust score: userHash =  {}, KTS = {}, userType =  {}", request.userHash, request.kycTrustScore, request.userType);
            if (request.kycTrustScore <= 0 || request.kycTrustScore > 100) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(KYC_TRUST_INCORRECT_VALUE, STATUS_ERROR));
            }
            TrustScoreData newTrustScoreData = new TrustScoreData(request.userHash,
                    request.kycTrustScore,
                    request.signature,
                    new Hash(kycServerPublicKey),
                    UserType.enumFromString(request.userType));
            if (!trustScoreCrypto.verifySignature(newTrustScoreData)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new Response(KYC_TRUST_SCORE_AUTHENTICATION_ERROR, STATUS_ERROR));
            }
            TrustScoreData oldTrustScoreData = trustScores.getByHash(request.userHash);
            if (oldTrustScoreData == null) {
                createBuckets(newTrustScoreData);
                trustScores.put(newTrustScoreData);
                kycTrustScoreResponse = new SetKycTrustScoreResponse(newTrustScoreData);
            } else {
                if (!oldTrustScoreData.getUserType().equals(UserType.enumFromString(request.userType))) {
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new Response(KYC_TRUST_DIFFERENT_TYPE, STATUS_ERROR));
                }

                oldTrustScoreData.setKycTrustScore(newTrustScoreData.getKycTrustScore());
                oldTrustScoreData.setSignature(newTrustScoreData.getSignature());
                oldTrustScoreData.setCreateTime(newTrustScoreData.getCreateTime());
                oldTrustScoreData.setKycServerPublicKey(newTrustScoreData.getKycServerPublicKey());
                trustScores.put(oldTrustScoreData);
                kycTrustScoreResponse = new SetKycTrustScoreResponse(oldTrustScoreData);
            }

            InsertEventRequest insertEventRequest = new InsertEventRequest();
            insertEventRequest.setEventDate(Instant.now());
            insertEventRequest.setUserHash(request.getUserHash());
            insertEventRequest.setInitialTrustScoreType(InitialTrustScoreType.KYC);
            insertEventRequest.setScore(request.getKycTrustScore());
            insertEventRequest.setSignerHash(new Hash(kycServerPublicKey));
            insertEventRequest.setSignature(request.getSignature());
            insertEventRequest.setUniqueIdentifier(new Hash(0));
            insertEventRequest.setEventType(EventType.INITIAL_EVENT);
            sendToBucketInitialTrustScoreEventsService(insertEventRequest);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(kycTrustScoreResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(KYC_TRUST_SCORE_ERROR, STATUS_ERROR));
        }
    }

    public ResponseEntity<IResponse> setUserZeroTrustFlag(SetUserZeroTrustFlagRequest request) {

        try {
            log.info("Setting Zero Trust Flag: userHash =  {}, ZTF = {}", request.getUserHash(), request.isZeroTrustFlag());

            TrustScoreData trustScoreData = trustScores.getByHash(request.getUserHash());
            if (trustScoreData == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
            } else {
                trustScoreData.setZeroTrustFlag(request.isZeroTrustFlag());
                trustScores.put(trustScoreData);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new SetUserZeroTrustFlagResponse(trustScoreData));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(ZERO_TRUST_FLAG_ERROR, STATUS_ERROR));
        }
    }

    public synchronized void addTransactionToTsCalculation(TransactionData transactionData) {
        try {
            if (EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial).contains(transactionData.getType()) || transactionData.getDspConsensusResult() == null ||
                    !transactionData.getDspConsensusResult().isDspConsensus()) {
                return;
            }

            LocalDate transactionConsensusDate = transactionData.getDspConsensusResult().getIndexingTime().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate currentDate = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (currentDate.equals(transactionConsensusDate)) {
                if (transactionData.getSenderHash() != null) {
                    addTransactionToUserTs(transactionData, transactionData.getSenderHash());
                }
                if (transactionData.getNodeHash() != null) {
                    addTransactionToUserTs(transactionData, transactionData.getNodeHash());
                }
            }
        } catch (Exception e) {
            log.error("Error at adding transaction to TS calculation", e);
        }

    }

    private void addTransactionToUserTs(TransactionData transactionData, Hash userHash) {
        TrustScoreData trustScoreData = trustScores.getByHash(userHash);

        if (trustScoreData == null) {
            log.error("Transaction can not be added to TS calculation: User {} doesn't exist", userHash);
            return;
        }

        BucketEventData bucketEventData
                = (BucketEventData) bucketEvents.getByHash(getBucketHashByUserHashAndEventType(userHash, EventType.TRANSACTION));
        if (bucketEventData == null) {
            log.error("Transaction can not be added to TS calculation: bucket event data doesn't exist for user {}", userHash);
            return;
        }

        if (bucketEventData.getEventDataHashToEventDataMap().get(transactionData.getHash()) != null) {
            log.debug("Transaction {} is already added to ts calculation", transactionData.getHash());
            return;
        }

        addToTransactionBucketsCalculation(trustScoreData, transactionData);

    }

    private void createBuckets(TrustScoreData trustScoreData) {
        try {
            for (EventType event : EventType.values()) {

                BucketEventData bucketEventData = BucketBuilder.createBucket(event, trustScoreData.getUserType(), trustScoreData.getUserHash());
                bucketEvents.put(bucketEventData);
                trustScoreData.getEventTypeToBucketHashMap().put(event, bucketEventData.getHash());
            }
        } catch (CotiRunTimeException e) {
            e.logMessage();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void initBuckets() {
        bucketTransactionService.init(rulesData);
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

        if (transactionData.getType() == TransactionType.Payment && transactionData.getAmount().doubleValue() > 0) {
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

        if (trustScoreData.getZeroTrustFlag() != null && trustScoreData.getZeroTrustFlag()) {
            return 0;
        }

        double eventsTrustScore = 10;

        for (IBucketEventService bucketEventService : bucketEventServiceList) {
            BucketEventData bucketEventData =
                    (BucketEventData) bucketEvents.getByHash(trustScoreData.getEventTypeToBucketHashMap().get(bucketEventService.getBucketEventType()));
            if (bucketEventData != null) {
                eventsTrustScore += bucketEventService.getBucketSumScore(bucketEventData);
                bucketEventData.setLastUpdate(DatesCalculation.setDateOnBeginningOfDay(new Date()));
            }
        }
        return Math.min(Math.max(eventsTrustScore, 0.1), 100.0);
    }

    private void updateUserTypeInBuckets(TrustScoreData trustScoreData) {
        for (Map.Entry<EventType, Hash> eventTypeToBucketHashEntry
                : trustScoreData.getEventTypeToBucketHashMap().entrySet()) {
            Hash bucketHash = eventTypeToBucketHashEntry.getValue();
            BucketEventData bucket = (BucketEventData) bucketEvents.getByHash(bucketHash);
            if (bucket != null) {
                bucket.setUserType(trustScoreData.getUserType());
                bucketEvents.put(bucket);
            }
        }
    }

    private RulesData loadRulesFromJsonFile() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream jsonConfigStream = new ClassPathResource("trustScoreRules.json").getInputStream();
            rulesData = objectMapper.readValue(jsonConfigStream, RulesData.class);
            log.debug(rulesData.toString());
        } catch (IOException e) {
            log.error("Error reading from JSON file", e);
            log.error("Shutting down!");
            System.exit(1);
        }
        return rulesData;
    }

    private IResponse sendToSuitableService(InsertEventRequest request) {
        switch (request.getEventType()) {
            case INITIAL_EVENT:
                return sendToBucketInitialTrustScoreEventsService(request);

            case HIGH_FREQUENCY_EVENTS:
                return sendToHighFrequencyEventScoreService(request);

            case BEHAVIOR_EVENT:
                return sendToBucketBehaviorEventsService(request);

            case NOT_FULFILMENT_EVENT:
                return sendToBucketNotFulfilmentEventsService(request);

            default:
                throw new IllegalArgumentException(ILLEGAL_EVENT_FROM_KYC_SERVER);
        }
    }

    private IResponse sendToBucketInitialTrustScoreEventsService(InsertEventRequest request) {

        InitialTrustScoreEventsData initialTrustScoreEventsData =
                new InitialTrustScoreEventsData(request);

        Hash bucketHash = getBucketHashByUserHashAndEventType(request);
        BucketInitialTrustScoreEventsData bucketInitialTrustScoreEventsData =
                (BucketInitialTrustScoreEventsData) bucketEvents.getByHash(bucketHash);

        bucketInitialTrustScoreEventsService.addEventToCalculations(
                initialTrustScoreEventsData,
                bucketInitialTrustScoreEventsData);

        bucketEvents.put(bucketInitialTrustScoreEventsData);
        return new SetInitialTrustScoreEventResponse(request.getUserHash(), request.getEventType(), request.getInitialTrustScoreType(), request.getScore());
    }

    private IResponse sendToHighFrequencyEventScoreService(InsertEventRequest request) {
        if (request.getHighFrequencyEventScoreType() == HighFrequencyEventScoreType.CHARGE_BACK) {
            ChargeBackEventsData chargeBackEventsData = new ChargeBackEventsData(request);

            Hash bucketHash = getBucketHashByUserHashAndEventType(request);
            BucketChargeBackEventsData bucketChargeBackEventsData = (BucketChargeBackEventsData) bucketEvents.getByHash(bucketHash);

            bucketChargeBackEventsService.addEventToCalculations(chargeBackEventsData, bucketChargeBackEventsData);
            bucketEvents.put(bucketChargeBackEventsData);

            Hash transactionDataHash = (request.getTransactionData() != null) ? request.getTransactionData().getHash() : null;
            return new SetHighFrequencyEventScoreResponse(request.getUserHash(), request.getEventType(), request.getHighFrequencyEventScoreType(), transactionDataHash);
        }
        return new Response(ILLEGAL_EVENT_FROM_KYC_SERVER, API_SERVER_ERROR);
    }

    private IResponse sendToBucketNotFulfilmentEventsService(InsertEventRequest request) {
        NotFulfilmentEventsData notFulfilmentEventsData = new NotFulfilmentEventsData(request);

        Hash bucketHash = getBucketHashByUserHashAndEventType(request);
        BucketNotFulfilmentEventsData bucketNotFulfilmentEventsData = (BucketNotFulfilmentEventsData) bucketEvents.getByHash(bucketHash);

        bucketNotFulfilmentEventsService.addEventToCalculations(notFulfilmentEventsData, bucketNotFulfilmentEventsData);

        try {
            bucketEvents.put(bucketNotFulfilmentEventsData);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return new SetNotFulfilmentEventScoreResponse(request.getUserHash(), request.getEventType(), request.getCompensableEventScoreType());
    }

    private IResponse sendToBucketBehaviorEventsService(InsertEventRequest request) {
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
        return new SetBehaviorEventResponse(request.getUserHash(), request.getEventType(), request.getBehaviorEventsScoreType(), transactionDataHash);
    }


    private Hash getBucketHashByUserHashAndEventType(InsertEventRequest request) {
        return getBucketHashByUserHashAndEventType(request.getUserHash(), request.getEventType());
    }

    private Hash getBucketHashByUserHashAndEventType(Hash userHash, EventType eventType) {
        return new Hash(ByteBuffer.allocate(userHash.getBytes().length + Integer.BYTES).
                put(userHash.getBytes()).putInt(eventType.getValue()).array());
    }
}