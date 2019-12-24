package io.coti.trustscore.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.crypto.ExpandedTransactionTrustScoreCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.GetUserTrustScoreResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.TransactionTrustScoreResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.config.rules.ScoreRules;
import io.coti.trustscore.crypto.*;
import io.coti.trustscore.data.AddressUserIndexData;
import io.coti.trustscore.data.UnlinkedAddressData;
import io.coti.trustscore.data.UserTrustScoreData;
import io.coti.trustscore.data.tsbuckets.*;
import io.coti.trustscore.data.tsenums.EventRequestType;
import io.coti.trustscore.data.tsenums.EventType;
import io.coti.trustscore.data.tsenums.UserType;
import io.coti.trustscore.data.tsevents.*;
import io.coti.trustscore.http.*;
import io.coti.trustscore.model.AddressUserIndex;
import io.coti.trustscore.model.Buckets;
import io.coti.trustscore.model.UnlinkedAddresses;
import io.coti.trustscore.model.UserTrustScores;
import io.coti.trustscore.services.interfaces.IBucketService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.trustscore.http.HttpStringConstants.*;

@Slf4j
@Service
@Data
public class TrustScoreService {

    @Value("${kycserver.public.key:}")
    private String[] kycServerPublicKeys;

    @Autowired
    private ExpandedTransactionTrustScoreCrypto expandedTransactionTrustScoreCrypto;
    @Autowired
    private SetKycTrustScoreCrypto setKycTrustScoreCrypto;
    @Autowired
    private TrustScoreUserTypeCrypto trustScoreUserTypeCrypto;
    @Autowired
    private InsertDocumentScoreCrypto insertDocumentScoreCrypto;
    @Autowired
    private InsertEventScoreCrypto insertEventScoreCrypto;
    @Autowired
    private InsertChargeBackFrequencyBasedScoreCrypto insertChargeBackFrequencyBasedScoreCrypto;
    @Autowired
    private SetUserZeroTrustFlagCrypto setUserZeroTrustFlagCrypto;
    @Autowired
    private InsertDebtBalanceBasedScoreCrypto insertDebtBalanceBasedScoreCrypto;
    @Autowired
    private InsertDepositBalanceBasedScoreCrypto insertDepositBalanceBasedScoreCrypto;

    @Autowired
    private UserTrustScores userTrustScores;
    @Autowired
    private Buckets buckets;
    @Autowired
    private AddressUserIndex addressUserIndex;
    @Autowired
    private UnlinkedAddresses unlinkedAddresses;

    @Autowired
    private BucketDebtBalanceBasedScoreService bucketDebtBalanceBasedScoreService;
    @Autowired
    private BucketDepositBalanceBasedScoreService bucketDepositBalanceBasedScoreService;
    @Autowired
    private BucketDocumentScoreService bucketDocumentScoreService;
    @Autowired
    private BucketEventScoreService bucketEventScoreService;
    @Autowired
    private BucketFrequencyBasedScoreService bucketFrequencyBasedScoreService;
    @Autowired
    private BucketTransactionScoreService bucketTransactionScoreService;
    @Autowired
    private BucketChargeBackFrequencyBasedScoreService bucketChargeBackFrequencyBasedScoreService;

    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private RollingReserveService rollingReserveService;

    @Autowired
    private GetTransactionTrustScoreRequestCrypto getTransactionTrustScoreRequestCrypto;

    private List<IBucketService> bucketScoreServiceList;
    private RulesData rulesData;
    private Map<Hash, Hash> lockUnlinkedAddressesHashMap = new ConcurrentHashMap<>();
    private Map<Hash, Hash> lockUserTrustScoresHashMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        log.info("{} is up", this.getClass().getSimpleName());
        bucketScoreServiceList = new ArrayList<>();

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

        Map<String, ScoreRules> classToScoreRulesMap = rulesData.getClassToScoreRulesMap();
        BucketDocumentScoreService.init(classToScoreRulesMap);
        BucketEventScoreService.init(classToScoreRulesMap);
        BucketFrequencyBasedScoreService.init(classToScoreRulesMap);
        BucketTransactionScoreService.init(classToScoreRulesMap);
        BucketChargeBackFrequencyBasedScoreService.init(classToScoreRulesMap);
        BucketDebtBalanceBasedScoreService.init(classToScoreRulesMap);
        BucketDepositBalanceBasedScoreService.init(classToScoreRulesMap);

        bucketScoreServiceList.add(bucketDocumentScoreService);
        bucketScoreServiceList.add(bucketEventScoreService);
        bucketScoreServiceList.add(bucketFrequencyBasedScoreService);
        bucketScoreServiceList.add(bucketTransactionScoreService);
        bucketScoreServiceList.add(bucketChargeBackFrequencyBasedScoreService);
        bucketScoreServiceList.add(bucketDebtBalanceBasedScoreService);
        bucketScoreServiceList.add(bucketDepositBalanceBasedScoreService);
    }

    public ResponseEntity<IResponse> setKycTrustScore(SetKycTrustScoreRequest request) {
        SetKycTrustScoreResponse kycTrustScoreResponse;

        log.info("Setting KYC trust score: userHash =  {}, KTS = {}, userType =  {}", request.getUserHash(), request.getKycTrustScore(), request.getUserType());
        if (request.getKycTrustScore() <= 0 || request.getKycTrustScore() > 100) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(KYC_TRUST_INCORRECT_VALUE, STATUS_ERROR));
        }
        if (!checkIfSignerInList(request.getSignerHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNKNOWN_SIGNER_ERROR, STATUS_ERROR));
        }
        if (!setKycTrustScoreCrypto.verifySignature(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(KYC_TRUST_SCORE_AUTHENTICATION_ERROR, STATUS_ERROR));
        }

        UserType userType = UserType.enumFromString(request.getUserType());
        ResponseEntity badResponse = null;
        synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, request.getUserHash()))) {
            UserTrustScoreData oldUserTrustScoreData = userTrustScores.getByHash(request.getUserHash());
            if (oldUserTrustScoreData == null) {
                UserTrustScoreData newUserTrustScoreData = new UserTrustScoreData(request.getUserHash(), userType);
                if (!createBuckets(newUserTrustScoreData)) {
                    badResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ERROR_CREATING_BUCKETS, STATUS_ERROR));
                } else {
                    userTrustScores.put(newUserTrustScoreData);
                }
            } else if (!oldUserTrustScoreData.getUserType().equals(userType)) {
                badResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(KYC_TRUST_DIFFERENT_TYPE, STATUS_ERROR));
            }

            if (badResponse == null) {
                BucketDocumentEventData bucketData = (BucketDocumentEventData) buckets.getByHash(getBucketHashByUserHashAndScoreType(request.getUserHash(), EventType.DOCUMENT_SCORE));
                KYCDocumentEventData kYCDocumentScoreData = new KYCDocumentEventData(request);
                bucketData.addScoreToBucketMap(kYCDocumentScoreData);
                bucketDocumentScoreService.addScoreToCalculations(kYCDocumentScoreData, bucketData);
                buckets.put(bucketData);
                insertFillQuestionnaireEvent(request.getUserHash());
            }
            removeLockFromLocksMap(lockUserTrustScoresHashMap, request.getUserHash());
        }

        if (badResponse != null) {
            return badResponse;
        }
        kycTrustScoreResponse = new SetKycTrustScoreResponse(request.getUserHash().toHexString(), request.getKycTrustScore());
        return ResponseEntity.status(HttpStatus.OK).body(kycTrustScoreResponse);
    }

    public ResponseEntity<IResponse> insertDocumentScore(InsertDocumentScoreRequest request) {
        if (!checkIfSignerInList(request.getSignerHash())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(UNKNOWN_SIGNER_ERROR, STATUS_ERROR));
        }
        if (!insertDocumentScoreCrypto.verifySignature(request)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(INSERT_DOCUMENT_AUTHENTICATION_ERROR, STATUS_ERROR));
        }

        DocumentEventData documentScoreData = null;
        try {
            documentScoreData = (DocumentEventData) request.getDocumentType().getEventClass().getDeclaredConstructor(InsertDocumentScoreRequest.class).newInstance(request);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(INSERT_DOCUMENT_SCORE_ERROR, STATUS_ERROR));
        }
        synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, request.getUserHash()))) {
            BucketDocumentEventData bucketData = (BucketDocumentEventData) buckets.getByHash(getBucketHashByUserHashAndScoreType(request.getUserHash(), EventType.DOCUMENT_SCORE));
            bucketData.addScoreToBucketMap(documentScoreData);
            bucketDocumentScoreService.addScoreToCalculations(documentScoreData, bucketData);
            buckets.put(bucketData);
            insertFillQuestionnaireEvent(request.getUserHash());
            removeLockFromLocksMap(lockUserTrustScoresHashMap, request.getUserHash());
        }

        InsertDocumentScoreResponse insertDocumentScoreResponse = new InsertDocumentScoreResponse(request.getUserHash().toHexString(),
                request.getDocumentType().toString(), request.getScore());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(insertDocumentScoreResponse);

    }

    public ResponseEntity<IResponse> insertEventScore(InsertEventScoreRequest request) {
        if (!checkIfSignerInList(request.getSignerHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNKNOWN_SIGNER_ERROR, STATUS_ERROR));
        }
        if (!insertEventScoreCrypto.verifySignature(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INSERT_EVENT_AUTHENTICATION_ERROR, STATUS_ERROR));
        }

        ResponseEntity badResponse = null;
        if (request.getEventType() != EventRequestType.CLAIM) {
            BehaviorEventData eventClaimScoreData = null;
            try {
                eventClaimScoreData = (BehaviorEventData) request.getEventType().getEventClass().getDeclaredConstructor(InsertEventScoreRequest.class).newInstance(request);
            } catch (Exception e) {
                log.error(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(INSERT_EVENT_SCORE_ERROR, STATUS_ERROR));
            }
            synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, request.getUserHash()))) {
                BucketBehaviorEventData bucketData = (BucketBehaviorEventData) buckets.getByHash(getBucketHashByUserHashAndScoreType(request.getUserHash(), EventType.EVENT_SCORE));
                if (bucketData.getEventDataHashToEventDataMap().get(request.getEventIdentifier()) != null) {
                    badResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(EVENT_EXIST, STATUS_ERROR));
                } else {
                    bucketData.addScoreToBucketMap(eventClaimScoreData);
                    bucketEventScoreService.addScoreToCalculations(eventClaimScoreData, bucketData);
                    buckets.put(bucketData);
                }
                removeLockFromLocksMap(lockUserTrustScoresHashMap, request.getUserHash());
            }
        } else {
            FrequencyBasedEventData frequencyBasedScoreData = null;
            try {
                frequencyBasedScoreData = (FrequencyBasedEventData) request.getEventType().getEventClass().getDeclaredConstructor(InsertEventScoreRequest.class).newInstance(request);
            } catch (Exception e) {
                log.error(e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(INSERT_EVENT_SCORE_ERROR, STATUS_ERROR));
            }
            synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, request.getUserHash()))) {
                BucketFrequencyBasedEventData bucketData = (BucketFrequencyBasedEventData) buckets.getByHash(getBucketHashByUserHashAndScoreType(request.getUserHash(), EventType.FREQUENCY_BASED_SCORE));
                if (bucketData.getEventDataHashToEventDataMap().get(request.getEventIdentifier()) != null) {
                    badResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(EVENT_EXIST, STATUS_ERROR));
                } else {
                    bucketData.addScoreToBucketMap(frequencyBasedScoreData);
                    bucketFrequencyBasedScoreService.addScoreToCalculations(frequencyBasedScoreData, bucketData);
                    buckets.put(bucketData);
                }
                removeLockFromLocksMap(lockUserTrustScoresHashMap, request.getUserHash());
            }
        }

        if (badResponse != null) {
            return badResponse;
        }
        InsertEventScoreResponse insertEventScoreResponse = new InsertEventScoreResponse(request.getUserHash().toHexString(),
                request.getEventType().toString(), request.getEventIdentifier().toHexString());
        return ResponseEntity.status(HttpStatus.CREATED).body(insertEventScoreResponse);
    }


    public ResponseEntity<IResponse> insertChargeBackFrequencyBasedScore(InsertChargeBackFrequencyBasedScoreRequest request) {
        if (!checkIfSignerInList(request.getSignerHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNKNOWN_SIGNER_ERROR, STATUS_ERROR));
        }
        if (!insertChargeBackFrequencyBasedScoreCrypto.verifySignature(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INSERT_EVENT_AUTHENTICATION_ERROR, STATUS_ERROR));
        }

        ChargeBackFrequencyBasedEventData chargeBackFrequencyBasedScoreData = new ChargeBackFrequencyBasedEventData(request);

        ResponseEntity badResponse = null;
        synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, request.getUserHash()))) {
            BucketChargeBackFrequencyBasedEventData bucketData = (BucketChargeBackFrequencyBasedEventData) buckets.getByHash(getBucketHashByUserHashAndScoreType(request.getUserHash(), EventType.CHARGEBACK_SCORE));

            if (bucketData.getUserType() != UserType.MERCHANT) {
                badResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(String.format(USER_NOT_MERCHANT, request.getUserHash()), STATUS_ERROR));
            } else if (bucketData.getEventDataHashToEventDataMap().get(request.getEventIdentifier()) != null) {
                badResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(EVENT_EXIST, STATUS_ERROR));
            } else {
                bucketData.addScoreToBucketMap(chargeBackFrequencyBasedScoreData);
                bucketChargeBackFrequencyBasedScoreService.addScoreToCalculations(chargeBackFrequencyBasedScoreData, bucketData);
                buckets.put(bucketData);
            }
            removeLockFromLocksMap(lockUserTrustScoresHashMap, request.getUserHash());
        }

        if (badResponse != null) {
            return badResponse;
        }
        InsertChargeBackFrequencyBasedScoreResponse insertChargeBackFrequencyBasedScoreResponse =
                new InsertChargeBackFrequencyBasedScoreResponse(request.getUserHash().toHexString(),
                        request.getEventIdentifier().toHexString(), request.getTransactionHash().toHexString(), request.getAmount().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(insertChargeBackFrequencyBasedScoreResponse);
    }

    private void insertFillQuestionnaireEvent(Hash userHash) {
        Hash eventIdentifier = new Hash(DatatypeConverter.printHexBinary(Instant.now().toString().getBytes()).toLowerCase());
        FillQuestionnaireBehaviorEventData fillQuestionnaireEventScoreData = new FillQuestionnaireBehaviorEventData(eventIdentifier);
        BucketBehaviorEventData bucketData = (BucketBehaviorEventData) buckets.getByHash(getBucketHashByUserHashAndScoreType(userHash, EventType.EVENT_SCORE));
        if (bucketData.getEventDataHashToEventDataMap().get(eventIdentifier) == null) {
            bucketData.addScoreToBucketMap(fillQuestionnaireEventScoreData);
            bucketEventScoreService.addScoreToCalculations(fillQuestionnaireEventScoreData, bucketData);
            buckets.put(bucketData);
        }
    }

    public ResponseEntity<IResponse> insertDebtBalanceBasedScore(InsertDebtBalanceBasedScoreRequest request) {
        if (!checkIfSignerInList(request.getSignerHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNKNOWN_SIGNER_ERROR, STATUS_ERROR));
        }
        if (!insertDebtBalanceBasedScoreCrypto.verifySignature(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INSERT_EVENT_AUTHENTICATION_ERROR, STATUS_ERROR));
        }

        BalanceBasedEventData balanceBasedScoreData = null;
        try {
            balanceBasedScoreData = (BalanceBasedEventData) request.getEventType().getEventClass().getDeclaredConstructor(InsertDebtBalanceBasedScoreRequest.class).newInstance(request);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(INSERT_EVENT_SCORE_ERROR, STATUS_ERROR));
        }

        ResponseEntity badResponse = null;
        synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, request.getUserHash()))) {
            BucketDebtBalanceBasedEventData bucketData = (BucketDebtBalanceBasedEventData) buckets.getByHash(getBucketHashByUserHashAndScoreType(request.getUserHash(), EventType.DEBT_BALANCE_BASED_SCORE));

            if (bucketData.getEventDataHashToEventDataMap().get(request.getEventIdentifier()) != null) {
                badResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(EVENT_EXIST, STATUS_ERROR));
            } else {
                bucketData.addScoreToBucketMap(balanceBasedScoreData);
                bucketDebtBalanceBasedScoreService.addScoreToCalculations(balanceBasedScoreData, bucketData);
                buckets.put(bucketData);
            }
            removeLockFromLocksMap(lockUserTrustScoresHashMap, request.getUserHash());
        }

        if (badResponse != null) {
            return badResponse;
        }
        InsertDebtBalanceBasedScoreResponse insertDebtBalanceBasedScoreResponse = new InsertDebtBalanceBasedScoreResponse(request.getUserHash().toHexString(),
                request.getEventType().toString(), request.getAmount().toString(), request.getEventIdentifier().toHexString(), request.getOtherUserHash().toHexString());
        return ResponseEntity.status(HttpStatus.CREATED).body(insertDebtBalanceBasedScoreResponse);
    }

    public ResponseEntity<IResponse> insertDepositBalanceBasedScore(InsertDepositBalanceBasedScoreRequest request) {
        if (!checkIfSignerInList(request.getSignerHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNKNOWN_SIGNER_ERROR, STATUS_ERROR));
        }
        if (!insertDepositBalanceBasedScoreCrypto.verifySignature(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INSERT_EVENT_AUTHENTICATION_ERROR, STATUS_ERROR));
        }

        BalanceBasedEventData balanceBasedScoreData = null;

        try {
            balanceBasedScoreData = (BalanceBasedEventData) request.getEventType().getEventClass().getDeclaredConstructor(InsertDepositBalanceBasedScoreRequest.class).newInstance(request);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(INSERT_EVENT_SCORE_ERROR, STATUS_ERROR));
        }

        ResponseEntity badResponse = null;
        synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, request.getUserHash()))) {
            BucketDepositBalanceBasedEventData bucketData = (BucketDepositBalanceBasedEventData) buckets.getByHash(getBucketHashByUserHashAndScoreType(request.getUserHash(), EventType.DEPOSIT_BALANCE_BASED_SCORE));
            if (bucketData.getEventDataHashToEventDataMap().get(request.getEventIdentifier()) != null) {
                badResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(EVENT_EXIST, STATUS_ERROR));
            }

            bucketData.addScoreToBucketMap(balanceBasedScoreData);
            bucketDepositBalanceBasedScoreService.addScoreToCalculations(balanceBasedScoreData, bucketData);
            buckets.put(bucketData);
            removeLockFromLocksMap(lockUserTrustScoresHashMap, request.getUserHash());
        }

        if (badResponse != null) {
            return badResponse;
        }
        InsertDepositBalanceBasedScoreResponse insertDepositBalanceBasedScoreResponse = new InsertDepositBalanceBasedScoreResponse(request.getUserHash().toHexString(),
                request.getEventType().toString(), request.getAmount().toString(), request.getEventIdentifier().toHexString());
        return ResponseEntity.status(HttpStatus.CREATED).body(insertDepositBalanceBasedScoreResponse);
    }

    public ResponseEntity<IResponse> setUserType(SetUserTypeRequest request) {
        log.info("Setting UserType: " + request.getUserHash() + "=" + request.getUserType());

        if (!checkIfSignerInList(request.getSignerHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(UNKNOWN_SIGNER_ERROR, STATUS_ERROR));
        }
        if (!trustScoreUserTypeCrypto.verifySignature(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(SET_USER_TYPE_AUTHENTICATION_ERROR, STATUS_ERROR));
        }

        UserType userType = UserType.enumFromString(request.getUserType());
        ResponseEntity badResponse = null;
        synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, request.getUserHash()))) {
            UserTrustScoreData userTrustScoreData = userTrustScores.getByHash(request.getUserHash());
            if (userTrustScoreData == null) {
                badResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(USER_HASH_IS_NOT_IN_DB, STATUS_ERROR));
            } else {
                if (!changingIsLegal(userTrustScoreData)) {
                    badResponse = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(USER_TYPE_ALREADY_UPDATED, STATUS_ERROR));
                } else {
                    userTrustScoreData.setUserType(userType);
                    userTrustScores.put(userTrustScoreData);
                    updateUserTypeInBuckets(userTrustScoreData);
                }
            }
            removeLockFromLocksMap(lockUserTrustScoresHashMap, request.getUserHash());
        }

        if (badResponse != null) {
            return badResponse;
        }
        SetUserTypeResponse setUserTypeResponse = new SetUserTypeResponse(userType, request.getUserHash());
        return ResponseEntity.status(HttpStatus.OK).body(setUserTypeResponse);
    }


    public ResponseEntity<IResponse> getUserTrustScore(Hash userHash) {
        UserTrustScoreData userTrustScoreData = userTrustScores.getByHash(userHash);
        if (userTrustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }
        double currentTrustScore = calculateUserTrustScore(userTrustScoreData);
        GetUserTrustScoreResponse getUserTrustScoreResponse = new GetUserTrustScoreResponse(userHash.toString(), currentTrustScore, userTrustScoreData.getUserType().toString());
        return ResponseEntity.status(HttpStatus.OK).body(getUserTrustScoreResponse);
    }


    public ResponseEntity<IResponse> getTransactionTrustScore(GetTransactionTrustScoreRequest getTransactionTrustScoreRequest) {

        if (!getTransactionTrustScoreRequestCrypto.verifySignature(getTransactionTrustScoreRequest)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST).body(new Response(BAD_SIGNATURE_ON_TRUST_SCORE_FOR_TRANSACTION));
        }

        Hash userHash = getTransactionTrustScoreRequest.getUserHash();
        Hash transactionHash = getTransactionTrustScoreRequest.getTransactionHash();
        UserTrustScoreData userTrustScoreData = userTrustScores.getByHash(userHash);
        if (userTrustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }

        double currentTransactionsTrustScore = calculateUserTrustScore(userTrustScoreData);
        ExpandedTransactionTrustScoreData expandedTransactionTrustScoreData = new ExpandedTransactionTrustScoreData(userHash, transactionHash, currentTransactionsTrustScore);
        expandedTransactionTrustScoreCrypto.signMessage(expandedTransactionTrustScoreData);
        TransactionTrustScoreData transactionTrustScoreData = new TransactionTrustScoreData(expandedTransactionTrustScoreData);
        TransactionTrustScoreResponseData transactionTrustScoreResponseData = new TransactionTrustScoreResponseData(transactionTrustScoreData);
        GetTransactionTrustScoreResponse getTransactionTrustScoreResponse = new GetTransactionTrustScoreResponse(transactionTrustScoreResponseData);
        return ResponseEntity.status(HttpStatus.OK).body(getTransactionTrustScoreResponse);
    }

    public ResponseEntity<IResponse> getUserTrustScoreComponents(Hash userHash) {
        UserTrustScoreData userTrustScoreData = userTrustScores.getByHash(userHash);
        if (userTrustScoreData == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
        }

        List<BucketData> bucketDataList = new ArrayList<>();
        for (IBucketService bucketService : bucketScoreServiceList) {
            BucketData bucketData =
                    (BucketData) buckets.getByHash(userTrustScoreData.getEventTypeToBucketHashMap().get(bucketService.getScoreType()));
            bucketDataList.add(bucketData);
        }

        GetUserTrustScoreComponentsResponse getUserTrustScoreComponentsResponse = new GetUserTrustScoreComponentsResponse(userTrustScoreData, bucketDataList);
        return ResponseEntity.status(HttpStatus.OK).body(getUserTrustScoreComponentsResponse);
    }

    public double calculateUserTrustScore(UserTrustScoreData userTrustScoreData) {

        if (userTrustScoreData.getZeroTrustFlag() != null && userTrustScoreData.getZeroTrustFlag()) {
            return 0;
        }

        double trustScore = 10;
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, userTrustScoreData.getHash()))) {
            for (IBucketService bucketService : bucketScoreServiceList) {
                BucketData bucketData = (BucketData) buckets.getByHash(userTrustScoreData.getEventTypeToBucketHashMap().get(bucketService.getScoreType()));
                if (bucketData != null) {
                    LocalDate previousUpdate = bucketData.getLastUpdate();
                    trustScore += bucketService.getBucketSumScore(bucketData);
                    if (!previousUpdate.equals(today)) {
                        buckets.put(bucketData);
                    }
                }
            }
            removeLockFromLocksMap(lockUserTrustScoresHashMap, userTrustScoreData.getHash());
        }
        return Math.min(Math.max(trustScore, 0.1), 100.0);
    }


    public ResponseEntity<IResponse> purgeUser(PurgeUserRequest request) {

        try {
            log.info("Purging: userHash =  {}", request.getUserHash());

            UserTrustScoreData userTrustScoreData = userTrustScores.getByHash(request.getUserHash());

            if (userTrustScoreData != null) {

                for (EventType eventType : userTrustScoreData.getEventTypeToBucketHashMap().keySet()) {
                    BucketData bucketData =
                            (BucketData) buckets.getByHash(userTrustScoreData.getEventTypeToBucketHashMap().get(eventType));
                    buckets.delete(bucketData);
                }

                userTrustScores.delete(userTrustScoreData);
            }

            rollingReserveService.purgeMerchantRollingReserveAddress(request.getUserHash());

            Consumer consumerRD = addressRecord -> {
                AddressUserIndexData addressUserIndexData = (AddressUserIndexData) addressRecord;
                if (addressUserIndexData.getUserHash().equals(request.getUserHash())) {
                    UnlinkedAddressData unlinkedAddressData = new UnlinkedAddressData(addressUserIndexData.getAddress());
                    unlinkedAddresses.put(unlinkedAddressData);
                    addressUserIndex.delete(addressUserIndexData);
                }
            };
            addressUserIndex.forEach(consumerRD);

            // does not restore balances maps for unlinked addresses

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new PurgeUserResponse(request.getUserHash()));

        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(PURGING_USER_ERROR, STATUS_ERROR));
        }
    }

    public ResponseEntity<IResponse> setUserZeroTrustFlag(SetUserZeroTrustFlagSignedRequest request) {

        if (!checkIfSignerInList(request.getSignerHash())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(UNKNOWN_SIGNER_ERROR, STATUS_ERROR));
        }
        if (!setUserZeroTrustFlagCrypto.verifySignature(request)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(ZERO_TRUST_FLAG_AUTHENTICATION_ERROR, STATUS_ERROR));
        }

        SetUserZeroTrustFlagRequest setUserZeroTrustFlagRequest = new SetUserZeroTrustFlagRequest();
        setUserZeroTrustFlagRequest.setUserHash(request.getUserHash());
        setUserZeroTrustFlagRequest.setZeroTrustFlag(request.isZeroTrustFlag());
        return doSetUserZeroTrustFlag(setUserZeroTrustFlagRequest);
    }

    public ResponseEntity<IResponse> setUserZeroTrustFlag(SetUserZeroTrustFlagRequest request) {
        return doSetUserZeroTrustFlag(request);
    }

    public ResponseEntity<IResponse> doSetUserZeroTrustFlag(SetUserZeroTrustFlagRequest request) {
        log.info("Setting Zero Trust Flag: userHash =  {}, ZTF = {}", request.getUserHash(), request.isZeroTrustFlag());

        ResponseEntity badResponse = null;
        synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, request.getUserHash()))) {
            UserTrustScoreData userTrustScoreData = userTrustScores.getByHash(request.getUserHash());
            if (userTrustScoreData == null) {
                badResponse = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(NON_EXISTING_USER_MESSAGE, STATUS_ERROR));
            } else {
                userTrustScoreData.setZeroTrustFlag(request.isZeroTrustFlag());
                userTrustScores.put(userTrustScoreData);
            }
            removeLockFromLocksMap(lockUserTrustScoresHashMap, request.getUserHash());
        }

        if (badResponse != null) {
            return badResponse;
        }
        return ResponseEntity.status(HttpStatus.OK).body(new SetUserZeroTrustFlagResponse(request));
    }

    public void addTransactionToTsCalculation(TransactionData transactionData) {
        try {
            if (EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial).contains(transactionData.getType()) || transactionData.getDspConsensusResult() == null ||
                    !transactionData.getDspConsensusResult().isDspConsensus()) {
                return;
            }

            LocalDate transactionConsensusDate = transactionData.getDspConsensusResult().getIndexingTime().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

            if (currentDate.equals(transactionConsensusDate)) {
                if (transactionData.getSenderHash() != null) {
                    addTransactionToUserTs(transactionData, transactionData.getSenderHash());
                }
                if (transactionData.getNodeHash() != null) {
                    addTransactionToUserTs(transactionData, transactionData.getNodeHash());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void addTransactionToUserTs(TransactionData transactionData, Hash userHash) {
        UserTrustScoreData userTrustScoreData = userTrustScores.getByHash(userHash);

        if (userTrustScoreData == null) {
            log.error("Transaction can not be added to TS calculation: User {} doesn't exist", userHash);
            return;
        }

        synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, userHash))) {
            BucketTransactionEventData bucketTransactionEventData
                    = (BucketTransactionEventData) buckets.getByHash(getBucketHashByUserHashAndScoreType(userHash, EventType.TRANSACTION));
            if (bucketTransactionEventData == null) {
                log.error("Transaction can not be added to TS calculation: bucket event data doesn't exist for user {}", userHash);
            } else if (bucketTransactionEventData.getEventDataHashToEventDataMap().get(transactionData.getHash()) != null) {
                log.debug("Transaction {} is already added to ts calculation", transactionData.getHash());
            } else {
                addToTransactionBucketsCalculation(userTrustScoreData, transactionData, bucketTransactionEventData);
            }
            removeLockFromLocksMap(lockUserTrustScoresHashMap, userHash);
        }
    }

    private boolean createBuckets(UserTrustScoreData usertrustScoreData) {
        try {
            for (EventType score : EventType.values()) {

                BucketData bucketData = (BucketData) score.getBucket().getDeclaredConstructor().newInstance();
                bucketData.setUserType(usertrustScoreData.getUserType());
                bucketData.setHash(getBucketHashByUserHashAndScoreType(usertrustScoreData.getHash(), score));

                buckets.put(bucketData);
                usertrustScoreData.getEventTypeToBucketHashMap().put(score, bucketData.getHash());
            }
            return true;
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            log.error(e.toString());
            return false;
        }
    }

    private void addToTransactionBucketsCalculation(UserTrustScoreData userTrustScoreData, TransactionData transactionData, BucketTransactionEventData bucketData) {
        TransactionEventData transactionScoreData = new TransactionEventData(transactionData, userTrustScoreData.getUserType());

        if (userTrustScoreData.getUserType() != UserType.FULL_NODE && userTrustScoreData.getUserType() != UserType.DSP_NODE && userTrustScoreData.getUserType() != UserType.TRUST_SCORE_NODE) {
            for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
                if (baseTransactionData instanceof ReceiverBaseTransactionData) {
                    AddressUserIndexData addressUserIndexData = addressUserIndex.getByHash(baseTransactionData.getAddressHash());
                    if (addressUserIndexData != null) {
                        UserTrustScoreData receiverUserTrustScoreData;
                        if (userTrustScoreData.getHash().equals(addressUserIndexData.getUserHash())) {
                            TransactionEventData receiverTransactionScoreData = new TransactionEventData(transactionData, baseTransactionData, userTrustScoreData.getUserType());
                            bucketTransactionScoreService.addScoreToCalculations(receiverTransactionScoreData, bucketData);
                        } else {
                            synchronized ((addLockToLockMap(lockUserTrustScoresHashMap, addressUserIndexData.getUserHash()))) {
                                BucketTransactionEventData receiverBucketTransactionEventData = null;
                                receiverUserTrustScoreData = userTrustScores.getByHash(addressUserIndexData.getUserHash());
                                if (receiverUserTrustScoreData != null) {
                                    receiverBucketTransactionEventData = (BucketTransactionEventData) buckets.getByHash(receiverUserTrustScoreData.getEventTypeToBucketHashMap().get(EventType.TRANSACTION));
                                    if (receiverBucketTransactionEventData != null) {
                                        TransactionEventData receiverTransactionScoreData = new TransactionEventData(transactionData, baseTransactionData, receiverUserTrustScoreData.getUserType());
                                        bucketTransactionScoreService.addScoreToCalculations(receiverTransactionScoreData, receiverBucketTransactionEventData);
                                        buckets.put(receiverBucketTransactionEventData);
                                    } else {
                                        log.error("Transaction can not be added to balance TS calculation: User {} doesn't exist", addressUserIndexData.getUserHash());
                                    }
                                }
                                removeLockFromLocksMap(lockUserTrustScoresHashMap, addressUserIndexData.getUserHash());
                            }
                        }
                    } else {
                        synchronized ((addLockToLockMap(lockUnlinkedAddressesHashMap, baseTransactionData.getAddressHash()))) {
                            UnlinkedAddressData unlinkedAddressData = unlinkedAddresses.getByHash(baseTransactionData.getAddressHash());
                            if (unlinkedAddressData == null) {
                                unlinkedAddressData = new UnlinkedAddressData(baseTransactionData.getAddressHash());
                            }
                            unlinkedAddressData.insertToDateToBalanceMap(transactionScoreData, baseTransactionData.getAmount());
                            unlinkedAddresses.put(unlinkedAddressData);
                            removeLockFromLocksMap(lockUnlinkedAddressesHashMap, baseTransactionData.getAddressHash());
                        }
                    }
                } else if (baseTransactionData instanceof InputBaseTransactionData) {
                    synchronized ((addLockToLockMap(lockUnlinkedAddressesHashMap, baseTransactionData.getAddressHash()))) {
                        UnlinkedAddressData unlinkedAddressData = unlinkedAddresses.getByHash(baseTransactionData.getAddressHash());
                        if (unlinkedAddressData != null) {
                            AddressUserIndexData addressUserIndexData = new AddressUserIndexData(baseTransactionData.getAddressHash(), userTrustScoreData.getHash());
                            addressUserIndex.put(addressUserIndexData);
                            transactionScoreData.setUnlinkedAddressData(unlinkedAddressData);
                            unlinkedAddresses.delete(unlinkedAddressData);
                            removeLockFromLocksMap(lockUnlinkedAddressesHashMap, baseTransactionData.getAddressHash());
                        }
                    }
                }
            }
        }

        bucketTransactionScoreService.addScoreToCalculations(transactionScoreData, bucketData);
        buckets.put(bucketData);

        if (userTrustScoreData.getUserType() == UserType.MERCHANT && transactionData.getType() == TransactionType.Payment && transactionData.getAmount().doubleValue() > 0) {
            addTransactionToChargeBackBucket(transactionData.getSenderHash(), transactionData);
        }
    }

    private void addTransactionToChargeBackBucket(Hash userHash, TransactionData transactionData) {
        Hash bucketHash = getBucketHashByUserHashAndScoreType(userHash, EventType.CHARGEBACK_SCORE);

        BucketChargeBackFrequencyBasedEventData bucketData = (BucketChargeBackFrequencyBasedEventData) buckets.getByHash(bucketHash);
        bucketChargeBackFrequencyBasedScoreService.addPaymentTransactionToCalculations(transactionData, bucketData);
        buckets.put(bucketData);
    }

    private void updateUserTypeInBuckets(UserTrustScoreData userTrustScoreData) {
        for (Map.Entry<EventType, Hash> eventTypeToBucketHashEntry : userTrustScoreData.getEventTypeToBucketHashMap().entrySet()) {
            Hash userTypeChangeBucketHash = eventTypeToBucketHashEntry.getValue();
            BucketData bucketData = (BucketData) buckets.getByHash(userTypeChangeBucketHash);
            if (bucketData != null) {
                bucketData.setUserType(userTrustScoreData.getUserType());
                buckets.put(bucketData);
            }
        }
    }

    private Hash getBucketHashByUserHashAndScoreType(Hash userHash, EventType eventType) {
        return new Hash(ByteBuffer.allocate(userHash.getBytes().length + Integer.BYTES).
                put(userHash.getBytes()).putInt(eventType.getValue()).array());
    }

    private boolean checkIfSignerInList(Hash signerHash) {
        return Arrays.asList(kycServerPublicKeys).contains(signerHash.toString());
    }

    private boolean changingIsLegal(UserTrustScoreData userTrustScoreData) {
        return userTrustScoreData.getUserType().equals(UserType.CONSUMER);
    }

    private Hash addLockToLockMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            locksIdentityMap.putIfAbsent(hash, hash);
            return locksIdentityMap.get(hash);
        }
    }

    private void removeLockFromLocksMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            Hash hashLock = locksIdentityMap.get(hash);
            if (hashLock != null) {
                locksIdentityMap.remove(hash);
            }
        }
    }


}