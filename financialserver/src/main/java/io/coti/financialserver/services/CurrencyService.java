package io.coti.financialserver.services;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.coti.basenode.crypto.CurrencyOriginatorCrypto;
import io.coti.basenode.crypto.GenerateTokenRequestCrypto;
import io.coti.basenode.crypto.GetTokenGenerationDataRequestCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.http.GenerateTokenRequest;
import io.coti.basenode.http.GetTokenGenerationDataRequest;
import io.coti.basenode.http.GetTokenGenerationDataResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.financialserver.data.CurrencyNameIndexData;
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
import java.util.Map;
import java.util.Optional;

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
    private GetTokenGenerationDataRequestCrypto getTokenGenerationDataRequestCrypto;
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

    private Map<Hash, Hash> concurrentUserHashes;
    private Map<String, String> pendingCurrencyNames;
    private Map<Hash, Hash> concurrentTransactionHashes;

    @Override
    public void init() {
        super.init();
        initConcurrentSets();

    }

    private void initConcurrentSets() {
        concurrentUserHashes = Maps.newConcurrentMap();
        pendingCurrencyNames = Maps.newConcurrentMap();
        concurrentTransactionHashes = Maps.newConcurrentMap();
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

    public ResponseEntity<IResponse> getUserTokenGenerationData(GetTokenGenerationDataRequest getTokenGenerationDataRequest) {
        if (getTokenGenerationDataRequest.getHash() == null || getTokenGenerationDataRequest.getSignature() == null || !getTokenGenerationDataRequestCrypto.verifySignature(getTokenGenerationDataRequest)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(getTokenGenerationDataRequest.getSenderHash());
        GetTokenGenerationDataResponse getTokenGenerationDataResponse = new GetTokenGenerationDataResponse();
        if (userTokenGenerationData == null) {
            return ResponseEntity.ok(getTokenGenerationDataResponse);
        }
        Map<Hash, Hash> userTransactionHashToCurrencyHashMap = userTokenGenerationData.getTransactionHashToCurrencyMap();
        userTransactionHashToCurrencyHashMap.entrySet().forEach(entry ->
                fillGetTokenGenerationDataResponse(getTokenGenerationDataResponse, entry));
        return ResponseEntity.ok(getTokenGenerationDataResponse);
    }

    private void fillGetTokenGenerationDataResponse(GetTokenGenerationDataResponse getTokenGenerationDataResponse, Map.Entry<Hash, Hash> userTransactionHashToCurrencyHashEntry) {
        Hash transactionHash = userTransactionHashToCurrencyHashEntry.getKey();
        Hash currencyHash = userTransactionHashToCurrencyHashEntry.getValue();
        if (currencyHash == null) {
            getTokenGenerationDataResponse.addUnusedConfirmedTransaction(transactionHash);
            return;
        }
        CurrencyData currencyData = pendingCurrencies.getByHash(currencyHash);
        if (currencyData != null) {
            getTokenGenerationDataResponse.addPendingTransactionHashToGeneratedCurrency(transactionHash, currencyData);
            return;
        }
        currencyData = currencies.getByHash(currencyHash);
        if (currencyData == null) {
            throw new CurrencyException(String.format("Unidentified currency hash: %s", currencyHash));
        }
        getTokenGenerationDataResponse.addCompletedTransactionHashToGeneratedCurrency(transactionHash, currencyData);
    }

    public ResponseEntity<IResponse> generateToken(GenerateTokenRequest generateTokenRequest) {
        Optional<ResponseEntity> validationResponseOpt = validateTokenGenerationRequest(generateTokenRequest);
        if (validationResponseOpt.isPresent()) {
            return validationResponseOpt.get();
        }
        try {
            occupyLocksAndValidateUniquenessAndAddToken(generateTokenRequest);
        } catch (CurrencyException e) {
            String error = e.getCause() != null ? String.format("%s : %s. %s", TOKEN_GENERATION_REQUEST_FAILURE, e.getMessage(), e.getCause()) : String.format("%s : %s.", TOKEN_GENERATION_REQUEST_FAILURE, e.getMessage());
            return ResponseEntity.ok(new Response(error));
        } catch (Exception e) {
            return ResponseEntity.ok(new Response(String.format("%s : %s.", TOKEN_GENERATION_REQUEST_FAILURE, e.getMessage()), STATUS_ERROR));
        }
        return ResponseEntity.ok(new Response(TOKEN_GENERATION_REQUEST_SUCCESS, STATUS_SUCCESS));
    }

    private synchronized void occupyLocksAndValidateUniquenessAndAddToken(GenerateTokenRequest generateTokenRequest) {
        OriginatorCurrencyData requestCurrencyData = generateTokenRequest.getOriginatorCurrencyData();
        String currencySymbol = requestCurrencyData.getSymbol();
        String currencyName = requestCurrencyData.getName();
        Hash userHash = generateTokenRequest.getHash();
        addLockToLocksMap(concurrentUserHashes, userHash);
        synchronized (concurrentUserHashes.get(userHash)) {
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
            pendingCurrencyNames.put(currencyName, currencyName);
            OriginatorCurrencyData requestedCurrencyData = generateTokenRequest.getOriginatorCurrencyData();
            CurrencyType currencyType = CurrencyType.REGULAR_CMD_TOKEN;
            CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now());
            CurrencyData currencyData = new CurrencyData(requestedCurrencyData, currencyTypeData);
            setSignedCurrencyTypeData(currencyData, currencyType);

            TransactionData tokenGenerationTransactionData = transactions.getByHash(requestTransactionHash);
            Hash requestCurrencyDataHash = currencyData.getHash();
            userTokenGenerationData.getTransactionHashToCurrencyMap().put(requestTransactionHash, requestCurrencyDataHash);
            if (tokenGenerationTransactionData.isTrustChainConsensus()) {
                addLockToLocksMap(concurrentTransactionHashes, requestTransactionHash);
                synchronized (requestTransactionHash) {
                    putCurrencyData(currencyData);
                    if (pendingCurrencies.getByHash(requestCurrencyDataHash) != null) {
                        //TODO 9/23/2019 tomer: Should not reach here
                        pendingCurrencies.delete(currencyData);
                    }
                }
                pendingCurrencyNames.remove(pendingCurrencyNames);
                concurrentTransactionHashes.remove(requestTransactionHash);
            } else {
                pendingCurrencies.put(currencyData);
            }
            userTokenGenerations.put(userTokenGenerationData);
        }
        removeLockFromLocksMap(concurrentUserHashes, userHash);
    }


    protected void setSignedCurrencyTypeData(CurrencyData currencyData, CurrencyType currencyType) {
        CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(currencyData.getHash(), currencyType, Instant.now());
        currencyTypeRegistrationCrypto.signMessage(currencyTypeRegistrationData);
        currencyData.setCurrencyTypeData(new CurrencyTypeData(currencyTypeRegistrationData));
    }

    private Optional<ResponseEntity> validateTokenGenerationRequest(GenerateTokenRequest generateTokenRequest) {
        if (!generateTokenRequestCrypto.verifySignature(generateTokenRequest)) {
            return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_GENERATION_REQUEST_INVALID_SIGNATURE, STATUS_ERROR)));
        }
        OriginatorCurrencyData currencyData = generateTokenRequest.getOriginatorCurrencyData();
        if (!currencyOriginatorCrypto.verifySignature(new CurrencyData(currencyData))) {
            return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_GENERATION_REQUEST_CURRENCY_DATA_INVALID_SIGNATURE, STATUS_ERROR)));
        }
        return Optional.empty();
    }

    private void validateTransactionAvailability(UserTokenGenerationData userTokenGenerationData, Hash requestTransactionHash, Hash currencyHash) {
        final Hash existingCurrencyHash = userTokenGenerationData.getTransactionHashToCurrencyMap().get(requestTransactionHash);
        if (existingCurrencyHash != null && existingCurrencyHash != currencyHash) {
            throw new CurrencyException(String.format("Transaction hash %s was already used", requestTransactionHash));
        }
    }

    private void validateCurrencyUniqueness(Hash currencyHash, String currencyName, String currencySymbol) {
        StringBuilder sb = new StringBuilder();

        if (pendingCurrencyNames.get(currencyName) != null || currencyNameIndexes.getByHash(new CurrencyNameIndexData(currencyName, currencyHash).getHash()) != null) {
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

    private void addLockToLocksMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            Hash hashLock = locksIdentityMap.get(hash);
            if (hashLock == null) {
                locksIdentityMap.put(hash, hash);
            }
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
        addLockToLocksMap(concurrentUserHashes, userHash);
    }

    public void removeUserFromHashLocks(Hash userHash) {
        removeLockFromLocksMap(concurrentUserHashes, userHash);
    }

    public Hash getUserHashLock(Hash userHash) {
        return concurrentUserHashes.get(userHash);
    }


    public void handleTCCConfirmedTransaction(TransactionData transactionData) {
        final Hash userHash = transactionData.getSenderHash();
        final Hash transactionHash = transactionData.getHash();
        final Hash currencyHash = userTokenGenerations.getByHash(userHash).getTransactionHashToCurrencyMap().get(transactionHash);
        if (currencyHash != null) {
            addLockToLocksMap(concurrentTransactionHashes, transactionHash);
            synchronized (concurrentTransactionHashes.get(transactionHash)) {
                CurrencyData pendingCurrency = pendingCurrencies.getByHash(currencyHash);
                if (pendingCurrency != null) {
                    pendingCurrencies.deleteByHash(currencyHash);
                    putCurrencyData(pendingCurrency);
                    pendingCurrencyNames.remove(pendingCurrency.getName());
                }
            }
            concurrentTransactionHashes.remove(transactionHash);
        }
    }

}