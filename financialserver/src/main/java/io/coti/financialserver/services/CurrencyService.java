package io.coti.financialserver.services;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.coti.basenode.crypto.CurrencyOriginatorCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.exceptions.CurrencyValidationException;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.financialserver.crypto.GenerateTokenRequestCrypto;
import io.coti.financialserver.crypto.GetUserTokensRequestCrypto;
import io.coti.financialserver.data.CurrencyNameIndexData;
import io.coti.financialserver.http.GenerateTokenRequest;
import io.coti.financialserver.http.GetTokenGenerationDataResponse;
import io.coti.financialserver.http.GetUserTokensRequest;
import io.coti.financialserver.http.data.GeneratedTokenResponseData;
import io.coti.financialserver.model.CurrencyNameIndexes;
import io.coti.financialserver.model.PendingCurrencies;
import io.coti.financialserver.model.UserTokenGenerations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class CurrencyService extends BaseNodeCurrencyService {

    private static final String GET_NATIVE_CURRENCY_ENDPOINT = "/currencies/native";
    @Value("${financialserver.seed}")
    private String seed;
    @Autowired
    private CurrencyNameIndexes currencyNameIndexes;
    @Autowired
    private GetUserTokensRequestCrypto getUserTokensRequestCrypto;
    @Autowired
    private GenerateTokenRequestCrypto generateTokenRequestCrypto;
    @Autowired
    private UserTokenGenerations userTokenGenerations;
    @Autowired
    private PendingCurrencies pendingCurrencies;
    @Autowired
    private CurrencyOriginatorCrypto currencyOriginatorCrypto;
    @Autowired
    private Transactions transactions;

    private Map<Hash, Hash> processingUserHashMap;
    private Set<String> processingCurrencyNameSet;
    private Set<String> processingCurrencySymbolSet;
    private Map<Hash, Hash> processingTransactionHashMap;

    private BlockingQueue<TransactionData> confirmedTransactionQueue;
    private Thread confirmedTransactionThread;

    @Override
    public void init() {
        super.init();
        initConcurrentSets();
        initConfirmedTransactionQueue();

    }

    private void initConcurrentSets() {
        processingUserHashMap = new ConcurrentHashMap<>();
        processingCurrencyNameSet = Sets.newConcurrentHashSet();
        processingCurrencySymbolSet = Sets.newConcurrentHashSet();
        processingTransactionHashMap = new ConcurrentHashMap<>();
    }

    private void initConfirmedTransactionQueue() {
        confirmedTransactionQueue = new LinkedBlockingQueue<>();
        confirmedTransactionThread = new Thread(() -> handleConfirmedTransaction());
        confirmedTransactionThread.start();
    }

    protected void addToConfirmedTransactionQueue(TransactionData transactionData) {
        try {
            confirmedTransactionQueue.put(transactionData);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleConfirmedTransaction() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TransactionData transactionData = confirmedTransactionQueue.take();
                handleTCCTransaction(transactionData);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updateCurrencyDataNameIndex(CurrencyData currencyData) {
        currencyNameIndexes.put(new CurrencyNameIndexData(currencyData.getName(), currencyData.getHash()));
    }

    @Override
    public void updateCurrencies() {
        try {
            CurrencyData nativeCurrencyData = getNativeCurrency();
            if (nativeCurrencyData == null) {
                String recoveryServerAddress = networkService.getRecoveryServerAddress();
                nativeCurrencyData = restTemplate.getForObject(recoveryServerAddress + GET_NATIVE_CURRENCY_ENDPOINT, CurrencyData.class);
                if (nativeCurrencyData == null) {
                    throw new CurrencyException("Native currency recovery failed. Recovery sent null native currency");
                } else {
                    putCurrencyData(nativeCurrencyData);
                    setNativeCurrencyData(nativeCurrencyData);
                }
            }
        } catch (CurrencyException e) {
            throw e;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new CurrencyException(String.format("Native currency recovery failed. %s: %s", e.getClass().getName(), new Gson().fromJson(e.getResponseBodyAsString(), Response.class).getMessage()));
        } catch (Exception e) {
            throw new CurrencyException(String.format("Native currency recovery failed. %s: %s", e.getClass().getName(), e.getMessage()));
        }
    }

    @Override
    public void putCurrencyData(CurrencyData currencyData) {
        super.putCurrencyData(currencyData);
        updateCurrencyDataNameIndex(currencyData);
    }

    public ResponseEntity<IResponse> getUserTokens(GetUserTokensRequest getUserTokensRequest) {
        try {
            if (!getUserTokensRequestCrypto.verifySignature(getUserTokensRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(getUserTokensRequest.getUserHash());
            GetTokenGenerationDataResponse getTokenGenerationDataResponse = new GetTokenGenerationDataResponse();
            if (userTokenGenerationData == null) {
                return ResponseEntity.ok(getTokenGenerationDataResponse);
            }
            Map<Hash, Hash> userTransactionHashToCurrencyHashMap = userTokenGenerationData.getTransactionHashToCurrencyMap();
            HashSet<GeneratedTokenResponseData> generatedTokens = new HashSet<>();
            userTransactionHashToCurrencyHashMap.entrySet().forEach(entry ->
                    fillGetTokenGenerationDataResponse(generatedTokens, entry));
            return ResponseEntity.ok(getTokenGenerationDataResponse);
        } catch (Exception e) {
            log.error("Error at getting user tokens: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private void fillGetTokenGenerationDataResponse(HashSet<GeneratedTokenResponseData> generatedTokens, Map.Entry<Hash, Hash> userTransactionHashToCurrencyHashEntry) {
        Hash transactionHash = userTransactionHashToCurrencyHashEntry.getKey();
        Hash currencyHash = userTransactionHashToCurrencyHashEntry.getValue();
        if (currencyHash == null) {
            generatedTokens.add(new GeneratedTokenResponseData(transactionHash, null, false));
            return;
        }
        CurrencyData currencyData = pendingCurrencies.getByHash(currencyHash);
        if (currencyData != null) {
            generatedTokens.add(new GeneratedTokenResponseData(transactionHash, currencyData, false));
            return;
        }
        currencyData = currencies.getByHash(currencyHash);
        if (currencyData == null) {
            throw new CurrencyException(String.format("Unidentified currency hash: %s", currencyHash));
        }
        generatedTokens.add(new GeneratedTokenResponseData(transactionHash, currencyData, true));
    }

    public ResponseEntity<IResponse> generateToken(GenerateTokenRequest generateTokenRequest) {
        try {
            validateTokenGenerationRequest(generateTokenRequest);
            occupyLocks(generateTokenRequest);
            occupyLocksAndValidateUniquenessAndAddToken(generateTokenRequest);
        } catch (CurrencyValidationException e) {
            String error = String.format("%s. Exception: %s", TOKEN_GENERATION_REQUEST_FAILURE, e.getMessageAndCause());
            return ResponseEntity.badRequest().body(new Response(error, STATUS_ERROR));
        } catch (CotiRunTimeException e) {
            String error = String.format("%s. Exception: %s", TOKEN_GENERATION_REQUEST_FAILURE, e.getMessageAndCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        } catch (Exception e) {
            String error = String.format("%s. Exception: %s", TOKEN_GENERATION_REQUEST_FAILURE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new Response(TOKEN_GENERATION_REQUEST_SUCCESS, STATUS_SUCCESS));
    }

    private void occupyLocks(GenerateTokenRequest generateTokenRequest) {
        addLockToProcessingMap(processingUserHashMap, generateTokenRequest.getSignerHash());
        addLockToProcessingMap(processingTransactionHashMap, generateTokenRequest.getTransactionHash());
        addLockToProcessingSet(processingCurrencyNameSet, generateTokenRequest.getOriginatorCurrencyData().getName());
        addLockToProcessingSet(processingCurrencySymbolSet, generateTokenRequest.getOriginatorCurrencyData().getSymbol());
    }

    private void occupyLocksAndValidateUniquenessAndAddToken(GenerateTokenRequest generateTokenRequest) {
        OriginatorCurrencyData requestCurrencyData = generateTokenRequest.getOriginatorCurrencyData();
        String currencySymbol = requestCurrencyData.getSymbol();
        String currencyName = requestCurrencyData.getName();
        Hash userHash = generateTokenRequest.getSignerHash();
        synchronized (processingUserHashMap.get(userHash)) {
            UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(userHash);
            Hash requestTransactionHash = generateTokenRequest.getTransactionHash();
            if (userTokenGenerationData == null) {
                throw new CurrencyException("Couldn't find Token generation data to match token generation request. Transaction was not propagated yet.");
            }
            try {
                Hash currencyHash = requestCurrencyData.calculateHash();
                validateTransactionAvailability(userTokenGenerationData, requestTransactionHash, currencyHash);
                validateCurrencyUniqueness(currencyHash, currencyName, currencySymbol);
            } catch (CurrencyException e) {
                throw new CurrencyException("Currency details are already in use. ", e);
            }
            processingCurrencyNameSet.add(currencyName);
            OriginatorCurrencyData requestedCurrencyData = generateTokenRequest.getOriginatorCurrencyData();
            CurrencyType currencyType = CurrencyType.REGULAR_CMD_TOKEN;
            CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now());
            CurrencyData currencyData = new CurrencyData(requestedCurrencyData, currencyTypeData);
            setSignedCurrencyTypeData(currencyData, currencyType);

            TransactionData tokenGenerationTransactionData = transactions.getByHash(requestTransactionHash);
            Hash requestCurrencyDataHash = currencyData.getHash();
            userTokenGenerationData.getTransactionHashToCurrencyMap().put(requestTransactionHash, requestCurrencyDataHash);
            if (tokenGenerationTransactionData.isTrustChainConsensus()) {
                addLockToProcessingMap(processingTransactionHashMap, requestTransactionHash);
                synchronized (requestTransactionHash) {
                    putCurrencyData(currencyData);
                    if (pendingCurrencies.getByHash(requestCurrencyDataHash) != null) {
                        //TODO 9/23/2019 tomer: Should not reach here
                        pendingCurrencies.delete(currencyData);
                    }
                }
                processingCurrencyNameSet.remove(processingCurrencyNameSet);
                processingTransactionHashMap.remove(requestTransactionHash);
            } else {
                pendingCurrencies.put(currencyData);
            }
            userTokenGenerations.put(userTokenGenerationData);
        }
        removeLockFromLocksMap(processingUserHashMap, userHash);
    }


    protected void setSignedCurrencyTypeData(CurrencyData currencyData, CurrencyType currencyType) {
        CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(currencyData.getHash(), currencyType, Instant.now());
        currencyTypeRegistrationCrypto.signMessage(currencyTypeRegistrationData);
        currencyData.setCurrencyTypeData(new CurrencyTypeData(currencyTypeRegistrationData));
    }

    private void validateTokenGenerationRequest(GenerateTokenRequest generateTokenRequest) {
        if (!generateTokenRequestCrypto.verifySignature(generateTokenRequest)) {
            throw new CurrencyValidationException(TOKEN_GENERATION_REQUEST_INVALID_SIGNATURE);
        }
        OriginatorCurrencyData currencyData = generateTokenRequest.getOriginatorCurrencyData();
        if (!currencyOriginatorCrypto.verifySignature(new CurrencyData(currencyData))) {
            throw new CurrencyValidationException(TOKEN_GENERATION_REQUEST_CURRENCY_DATA_INVALID_SIGNATURE);
        }
    }

    private void validateTransactionAvailability(UserTokenGenerationData userTokenGenerationData, Hash requestTransactionHash, Hash currencyHash) {
        final Hash existingCurrencyHash = userTokenGenerationData.getTransactionHashToCurrencyMap().get(requestTransactionHash);
        if (existingCurrencyHash != null && existingCurrencyHash != currencyHash) {
            throw new CurrencyException(String.format("Transaction hash %s was already used", requestTransactionHash));
        }
    }

    private void validateCurrencyUniqueness(Hash currencyHash, String currencyName, String currencySymbol) {
        StringBuilder sb = new StringBuilder();

        if (processingCurrencyNameSet.contains(currencyName) || currencyNameIndexes.getByHash(new CurrencyNameIndexData(currencyName, currencyHash).getHash()) != null) {
            sb.append("Currency name ").append(currencyName).append(" is already in use. ");
        }
        //TODO 9/23/2019 astolia: make sure removing/putting to these collections is in sync with this method.
        if (pendingCurrencies.getByHash(currencyHash) != null || currencies.getByHash(currencyHash) != null) {
            sb.append("Currency symbol ").append(currencySymbol).append(" is already in use. ");
        }
        if (sb.length() != 0) {
            throw new CurrencyException(sb.toString());
        }
    }

    private void addLockToProcessingMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            locksIdentityMap.putIfAbsent(hash, hash);
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

    public void addUserToHashLocks(Hash userHash) {
        addLockToProcessingMap(processingUserHashMap, userHash);
    }

    public void removeUserFromHashLocks(Hash userHash) {
        removeLockFromLocksMap(processingUserHashMap, userHash);
    }

    public Hash getUserHashLock(Hash userHash) {
        return processingUserHashMap.get(userHash);
    }

    public void addLockToProcessingSet(Set<String> lockProcessingSet, String lock) {
        synchronized (lockProcessingSet) {
            if (lockProcessingSet.contains(lock)) {
                throw new CurrencyException(String.format("%s is in progress", lock));
            } else {
                lockProcessingSet.add(lock);
            }
        }
    }

    public void handleTCCTransaction(TransactionData transactionData) {
        final Hash userHash = transactionData.getSenderHash();
        final Hash transactionHash = transactionData.getHash();
        final Hash currencyHash = userTokenGenerations.getByHash(userHash).getTransactionHashToCurrencyMap().get(transactionHash);
        if (currencyHash != null) {
            addLockToProcessingMap(processingTransactionHashMap, transactionHash);
            synchronized (processingTransactionHashMap.get(transactionHash)) {
                CurrencyData pendingCurrency = pendingCurrencies.getByHash(currencyHash);
                if (pendingCurrency != null) {
                    pendingCurrencies.deleteByHash(currencyHash);
                    putCurrencyData(pendingCurrency);
                    processingCurrencyNameSet.remove(pendingCurrency.getName());
                }
            }
            processingTransactionHashMap.remove(transactionHash);
        }
    }

}