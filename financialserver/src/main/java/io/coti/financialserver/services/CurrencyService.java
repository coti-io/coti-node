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
import java.util.function.Consumer;

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
    private Map<String, String> concurrentCurrencySymbols; //TODO 9/19/2019 astolia: change to set?
    private Map<String, String> concurrentCurrencyNames; //TODO 9/19/2019 astolia: change to set?
    private Map<Hash, Hash> concurrentTransactionHashes;

    @Override
    public void init() {
        super.init();
        initConcurrentSets();

    }

    private void initConcurrentSets() {
        concurrentUserHashes = Maps.newConcurrentMap();
        concurrentCurrencySymbols = Maps.newConcurrentMap();
        concurrentCurrencyNames = Maps.newConcurrentMap();
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
                fillGetTokenGenerationDataResponse(getTokenGenerationDataResponse, userTransactionHashToCurrencyHashMap, entry));
        return ResponseEntity.ok(getTokenGenerationDataResponse);
    }

    private void fillGetTokenGenerationDataResponse(GetTokenGenerationDataResponse getTokenGenerationDataResponse, Map<Hash, Hash> userTransactionHashToCurrencyHashMap, Map.Entry<Hash, Hash> userTransactionHashToCurrencyHashEntry) {
        Hash transactionHash = userTransactionHashToCurrencyHashEntry.getKey();
        if (userTransactionHashToCurrencyHashMap.get(transactionHash) == null) {
            getTokenGenerationDataResponse.addUnusedConfirmedTransaction(transactionHash);
            return;
        }
        Hash currencyHash = userTransactionHashToCurrencyHashMap.get(transactionHash);
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
        CurrencyData currencyData = generateTokenRequest.getCurrencyData();
        CurrencyType currencyType = CurrencyType.REGULAR_CMD_TOKEN;
        setSignedCurrencyTypeData(currencyData, currencyType);

        occupyLocksAndValidateUniquenessAndAddToken(generateTokenRequest, request -> {
            CurrencyData requestedCurrencyData = request.getCurrencyData();
            try {
                validateCurrencyUniqueness(requestedCurrencyData);
            } catch (CurrencyException e) {
                throw new CurrencyException("Currency details are already in use. ", e);
            }
            Hash requestUserHash = request.getHash();
            UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(requestUserHash);
            if (userTokenGenerationData == null) {
                throw new CurrencyException("Couldn't find Token generation data to match token generation request. Transaction was not propagated yet.");
            }
            Hash requestTransactionHash = request.getTransactionHash();
            TransactionData tokenGenerationTransactionData = transactions.getByHash(requestTransactionHash);
            Hash requestCurrencyDataHash = requestedCurrencyData.getHash();
            if (tokenGenerationTransactionData.isTrustChainConsensus()) {
                addLockToLocksMap(concurrentTransactionHashes, requestTransactionHash);
                synchronized (requestTransactionHash) {
                    if (pendingCurrencies.getByHash(requestCurrencyDataHash) != null) {
                        pendingCurrencies.delete(requestedCurrencyData);
                    }
                    putCurrencyData(request.getCurrencyData());
                    userTokenGenerationData.getTransactionHashToCurrencyMap().put(request.getTransactionHash(), requestCurrencyDataHash);
                }
            } else {

                pendingCurrencies.put(requestedCurrencyData);
                userTokenGenerationData.getTransactionHashToCurrencyMap().put(request.getTransactionHash(), requestCurrencyDataHash);
            }

        });
        //TODO 9/20/2019 astolia: what is the expected response??
        return null;//ResponseEntity.ok();
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
        CurrencyData currencyData = generateTokenRequest.getCurrencyData();
        if (!currencyOriginatorCrypto.verifySignature(currencyData)) {
            return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_GENERATION_REQUEST_CURRENCY_DATA_INVALID_SIGNATURE, STATUS_ERROR)));
        }
        CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(currencyData);
        if (!currencyTypeRegistrationCrypto.verifySignature(currencyTypeRegistrationData)) {
            return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_GENERATION_REQUEST_CURRENCY_TYPE_DATA_INVALID_SIGNATURE, STATUS_ERROR)));
        }
        return Optional.empty();
    }


    private void validateCurrencyUniqueness(CurrencyData currencyData) {
        StringBuilder sb = new StringBuilder();
        if(currencyNameIndexes.getByHash(new CurrencyNameIndexData(currencyData.getName(),currencyData.getHash()).getHash()) != null){
            sb.append("Currency name ").append(currencyData.getName()).append(" is already in use. ");
        }
        if(currencySymbolIndexes.getByHash(new CurrencySymbolIndexData(currencyData.getSymbol(),currencyData.getHash()).getHash()) != null){
            sb.append("Currency symbol ").append(currencyData.getSymbol()).append(" is already in use. ");
        }
        if(sb.length() != 0){
            throw new CurrencyException(sb.toString());
        }
    }

    //TODO 9/20/2019 astolia: need to sync flow of handle propagated transactions?????
    private synchronized void occupyLocksAndValidateUniquenessAndAddToken(GenerateTokenRequest generateTokenRequest, Consumer<GenerateTokenRequest> consumer) {
        Hash userHash = generateTokenRequest.getHash();
        CurrencyData requestCurrencyData = generateTokenRequest.getCurrencyData();
        String currencySymbol = requestCurrencyData.getSymbol();
        String currencyName = requestCurrencyData.getName();
        addLockToLocksMap(concurrentUserHashes, userHash);
        synchronized (concurrentUserHashes.get(userHash)) {
            concurrentCurrencySymbols.put(currencySymbol, currencySymbol);
            concurrentCurrencyNames.put(currencyName, currencyName);
            consumer.accept(generateTokenRequest);
        }
    }

    //TODO 9/20/2019 astolia: make sure no issues here.
    private void addLockToLocksMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            Hash hashLock = locksIdentityMap.get(hash);
            if (hashLock == null) {
                locksIdentityMap.put(hash, hash);
            }
        }
    }

    public void addUserHashLock(Hash userHash) {
        synchronized (concurrentUserHashes) {
            Hash hashLock = concurrentUserHashes.get(userHash);
            if (hashLock == null) {
                concurrentUserHashes.put(userHash, userHash);
            }
        }
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
                    releaseRelatedToPendingCurrency(pendingCurrency, transactionHash);
                }
            }
        }
    }

    private void releaseRelatedToPendingCurrency(CurrencyData pendingCurrency, Hash transactionHash) {
        concurrentCurrencySymbols.remove(pendingCurrency.getSymbol());
        concurrentCurrencyNames.remove(pendingCurrency.getName());
        concurrentTransactionHashes.remove(transactionHash);
    }

}