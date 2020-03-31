package io.coti.financialserver.services;

import com.google.gson.Gson;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.exceptions.CurrencyValidationException;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.interfaces.IMintingService;
import io.coti.financialserver.crypto.GetUserTokensRequestCrypto;
import io.coti.financialserver.http.*;
import io.coti.financialserver.http.data.GeneratedTokenResponseData;
import io.coti.financialserver.http.data.GetCurrencyResponseData;
import io.coti.financialserver.model.UserTokenGenerations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class CurrencyService extends BaseNodeCurrencyService {

    private static final String GET_NATIVE_CURRENCY_ENDPOINT = "/currencies/native";
    private static final String EXCEPTION_MESSAGE = "%s. Exception: %s";
    private final Map<Hash, Hash> lockUserHashMap = new ConcurrentHashMap<>();
    private final Map<Hash, Hash> lockTransactionHashMap = new ConcurrentHashMap<>();
    @Value("${currency.genesis.address}")
    private Hash currencyGenesisAddress;
    @Autowired
    private GetUserTokensRequestCrypto getUserTokensRequestCrypto;
    @Autowired
    private UserTokenGenerations userTokenGenerations;
    @Autowired
    private OriginatorCurrencyCrypto originatorCurrencyCrypto;
    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private FeeService feeService;
    @Autowired
    private IMintingService mintingService;
    private BlockingQueue<TransactionData> tokenGenerationTransactionQueue;
    private Thread tokenGenerationTransactionThread;
    private final Object lock = new Object();

    @Override
    public void init() {
        super.init();
        initQueuesAndThreads();
    }

    private void initQueuesAndThreads() {
        tokenGenerationTransactionQueue = new LinkedBlockingQueue<>();
        tokenGenerationTransactionThread = new Thread(this::handlePropagatedTokenGenerationTransactions);
        tokenGenerationTransactionThread.start();
    }

    private void addToTransactionQueue(BlockingQueue<TransactionData> queue, TransactionData transactionData) {
        try {
            queue.put(transactionData);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for insertion of transaction {} into blocking queue.", transactionData.getHash());
            Thread.currentThread().interrupt();
        }
    }

    public void addToTokenGenerationTransactionQueue(TransactionData transactionData) {
        addToTransactionQueue(tokenGenerationTransactionQueue, transactionData);
    }

//    @Override
//    public void updateCurrencies() {
//        try {
//            CurrencyData nativeCurrencyData = getNativeCurrency();
//            if (nativeCurrencyData == null) {
//                String recoveryServerAddress = networkService.getRecoveryServerAddress();
//                nativeCurrencyData = restTemplate.getForObject(recoveryServerAddress + GET_NATIVE_CURRENCY_ENDPOINT, CurrencyData.class);
//                if (nativeCurrencyData == null) {
//                    throw new CurrencyException("Native currency recovery failed. Recovery sent null native currency");
//                } else {
//                    putCurrencyData(nativeCurrencyData);
//                    setNativeCurrencyData(nativeCurrencyData);
//                }
//            }
//        } catch (CurrencyException e) {
//            throw e;
//        } catch (HttpClientErrorException | HttpServerErrorException e) {
//            throw new CurrencyException(String.format("Native currency recovery failed. %s: %s", e.getClass().getName(), new Gson().fromJson(e.getResponseBodyAsString(), Response.class).getMessage()));
//        } catch (Exception e) {
//            throw new CurrencyException(String.format("Native currency recovery failed. %s: %s", e.getClass().getName(), e.getMessage()));
//        }
//    }


    public ResponseEntity<IResponse> getUserTokens(GetUserTokensRequest getUserTokensRequest) {
        try {
            if (!getUserTokensRequestCrypto.verifySignature(getUserTokensRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(getUserTokensRequest.getUserHash());
            GetTokenGenerationDataResponse getTokenGenerationDataResponse = new GetTokenGenerationDataResponse();
            HashSet<GeneratedTokenResponseData> generatedTokens = new HashSet<>();
            getTokenGenerationDataResponse.setGeneratedTokens(generatedTokens);
            if (userTokenGenerationData == null) {
                return ResponseEntity.ok(getTokenGenerationDataResponse);
            }
            Map<Hash, Hash> userTransactionHashToCurrencyHashMap = userTokenGenerationData.getTransactionHashToCurrencyMap();
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
//        CurrencyData pendingCurrencyData = pendingCurrencies.getByHash(currencyHash);
//        if (pendingCurrencyData != null) {
//            generatedTokens.add(new GeneratedTokenResponseData(transactionHash, pendingCurrencyData, false));
//            return;
//        }
        CurrencyData currencyData = currencies.getByHash(currencyHash);
        if (currencyData == null) {
            throw new CurrencyException(String.format("Unidentified currency hash: %s", currencyHash));
        }
        GeneratedTokenResponseData generatedTokenResponseData = new GeneratedTokenResponseData(transactionHash, currencyData, true);

        BigDecimal genesisAddressBalance = balanceService.getBalance(currencyGenesisAddress, currencyHash);
        BigDecimal mintedAmount = BigDecimal.ZERO;
        if (genesisAddressBalance != null) {
            mintedAmount = currencyData.getTotalSupply().subtract(genesisAddressBalance);
        }
        generatedTokenResponseData.getToken().setMintedAmount(mintedAmount);
        generatedTokenResponseData.getToken().setRequestedMintingAmount(mintingService.getTokenAllocatedAmount(currencyHash).subtract(mintedAmount));
        generatedTokens.add(generatedTokenResponseData);
    }

    public ResponseEntity<IResponse> getTokenGenerationFee(GenerateTokenFeeRequest generateTokenRequest) {
        Hash currencyHash;
        try {
            OriginatorCurrencyData originatorCurrencyData = generateTokenRequest.getOriginatorCurrencyData();
            CurrencyTypeData currencyTypeData = generateTokenRequest.getCurrencyTypeData();
            String currencyName = originatorCurrencyData.getName();
            currencyHash = originatorCurrencyData.calculateHash();
            validateCurrencyUniqueness(currencyHash, currencyName);
            CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(currencyHash, currencyTypeData);
            if (!originatorCurrencyCrypto.verifySignature(originatorCurrencyData) || !currencyTypeRegistrationCrypto.verifySignature(currencyTypeRegistrationData)) {
                throw new CurrencyValidationException(TOKEN_GENERATION_REQUEST_INVALID_SIGNATURE);
            }
        } catch (CurrencyValidationException e) {
            String error = String.format(EXCEPTION_MESSAGE, TOKEN_GENERATION_REQUEST_FAILURE, e.getMessageAndCause());
            return ResponseEntity.badRequest().body(new Response(error, STATUS_ERROR));
        } catch (CotiRunTimeException e) {
            String error = String.format(EXCEPTION_MESSAGE, TOKEN_GENERATION_REQUEST_FAILURE, e.getMessageAndCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        } catch (Exception e) {
            String error = String.format(EXCEPTION_MESSAGE, TOKEN_GENERATION_REQUEST_FAILURE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        }

        return feeService.createTokenGenerationFee(generateTokenRequest);
    }

//    private void removeOccupyLocksFromProcessingSets(GenerateTokenRequest generateTokenRequest) {
//        removeLockFromProcessingSet(processingCurrencySymbolSet, generateTokenRequest.getOriginatorCurrencyData().getSymbol());
//        removeLockFromProcessingSet(processingCurrencyNameSet, generateTokenRequest.getOriginatorCurrencyData().getName());
//    }
//
//    private void occupyLocksToProcessingSets(GenerateTokenRequest generateTokenRequest) {
//        addLockToProcessingSet(processingCurrencyNameSet, generateTokenRequest.getOriginatorCurrencyData().getName());
//        addLockToProcessingSet(processingCurrencySymbolSet, generateTokenRequest.getOriginatorCurrencyData().getSymbol());
//    }

    private Hash validateUniquenessAndAddToken(GenerateTokenRequest generateTokenRequest) {   // todo move it to base class and do generation
        OriginatorCurrencyData originatorCurrencyData = generateTokenRequest.getOriginatorCurrencyData();
        String currencyName = originatorCurrencyData.getName();
        Hash userHash = generateTokenRequest.getSignerHash();
        CurrencyData currencyData;
        boolean tokenConfirmed = false;
        synchronized (addLockToLockMap(lockUserHashMap, userHash)) {
            UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(userHash);
            if (userTokenGenerationData == null) {
                throw new CurrencyException("Couldn't find Token generation data to match token generation request. Transaction was not propagated yet.");
            }
            Hash requestTransactionHash = generateTokenRequest.getTransactionHash();
            Hash currencyHash = originatorCurrencyData.calculateHash();

//            validateTransactionAvailability(userTokenGenerationData, requestTransactionHash);
            validateTransactionAmount(originatorCurrencyData, requestTransactionHash);
            validateCurrencyUniqueness(currencyHash, currencyName);

            CurrencyType currencyType = CurrencyType.REGULAR_CMD_TOKEN;
            CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now());
//            setSignedCurrencyTypeData(currencyData, currencyType);
//            currencyRegistrarCrypto.signMessage(currencyData);

            synchronized (addLockToLockMap(lockTransactionHashMap, requestTransactionHash)) {
                TransactionData tokenGenerationTransactionData = transactions.getByHash(requestTransactionHash);
                TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData = null;   //todo get TokenGenerationFeeBaseTransactionData
                currencyData = new CurrencyData(tokenGenerationFeeBaseTransactionData.getServiceData().getOriginatorCurrencyData(),
                        tokenGenerationFeeBaseTransactionData.getServiceData().getCurrencyTypeData(),
                        tokenGenerationTransactionData.getDspConsensusResult().getIndexingTime(),
                        requestTransactionHash, null);

                Hash requestCurrencyDataHash = currencyData.getHash();
                userTokenGenerationData.getTransactionHashToCurrencyMap().put(requestTransactionHash, requestCurrencyDataHash);
                if (transactionHelper.isConfirmed(tokenGenerationTransactionData)) {
                    putCurrencyData(currencyData);
                    tokenConfirmed = true;
                }
                removeLockFromLocksMap(lockTransactionHashMap, requestTransactionHash);
            }
            userTokenGenerations.put(userTokenGenerationData);
            removeLockFromLocksMap(lockUserHashMap, generateTokenRequest.getSignerHash());
        }
        if (tokenConfirmed) {
//            sendGeneratedToken(currencyData);
        }

        return currencyData.getHash();
    }

    private void validateTransactionAmount(OriginatorCurrencyData requestCurrencyData, Hash requestTransactionHash) {
        TransactionData tokenGenerationTransactionData = transactions.getByHash(requestTransactionHash);
        BaseTransactionData tokenServiceFeeData = tokenGenerationTransactionData.getBaseTransactions().stream()
                .filter(baseTransactionData -> baseTransactionData instanceof TokenFeeBaseTransactionData).findFirst().get();

        if (!tokenServiceFeeData.getAmount().equals(feeService.calculateTokenGenerationFee(requestCurrencyData.getTotalSupply()))) {
            throw new CurrencyException(String.format("The token generation fees in the transaction %s is not correct", requestTransactionHash));
        }
    }

    private Hash addLockToLockMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (lock) {
            locksIdentityMap.putIfAbsent(hash, hash);
            return locksIdentityMap.get(hash);
        }
    }

    private void removeLockFromLocksMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (lock) {
            Hash hashLock = locksIdentityMap.get(hash);
            if (hashLock != null) {
                locksIdentityMap.remove(hash);
            }
        }
    }

//    private void addLockToProcessingSet(Set<String> lockProcessingSet, String lock) {
//        synchronized (lockProcessingSet) {
//            if (lockProcessingSet.contains(lock)) {
//                throw new CurrencyException(String.format("%s is in progress", lock));
//            } else {
//                lockProcessingSet.add(lock);
//            }
//        }
//    }
//
//    private void removeLockFromProcessingSet(Set<String> lockProcessingSet, String lock) {
//        synchronized (lockProcessingSet) {
//            lockProcessingSet.remove(lock);
//        }
//    }

    private void handlePropagatedTokenGenerationTransactions() {  // todo move it to base method and use
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TransactionData tokenGenerationTransaction = tokenGenerationTransactionQueue.take();
                Hash userHash = tokenGenerationTransaction.getSenderHash();
                synchronized (addLockToLockMap(lockUserHashMap, userHash)) {
                    UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(userHash);
                    if (userTokenGenerationData == null) {
                        Map<Hash, Hash> transactionHashToCurrencyMap = new HashMap<>();
                        transactionHashToCurrencyMap.put(tokenGenerationTransaction.getHash(), null);
                        userTokenGenerations.put(new UserTokenGenerationData(userHash, transactionHashToCurrencyMap));
                    } else {
                        userTokenGenerationData.getTransactionHashToCurrencyMap().put(tokenGenerationTransaction.getHash(), null);
                        userTokenGenerations.put(userTokenGenerationData);
                    }
                    removeLockFromLocksMap(lockUserHashMap, userHash);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    public ResponseEntity<IResponse> getCurrenciesForWallet(GetCurrenciesRequest getCurrenciesRequest) {
        List<GetCurrencyResponseData> tokenDetails = new ArrayList<>();
        getCurrenciesRequest.getTokenHashes().forEach(tokenHash -> {
            if (!tokenHash.equals(nativeCurrencyData.getHash())) {
                CurrencyData tokenData = getCurrencyFromDB(tokenHash);
                if (tokenData != null) {
                    tokenDetails.add(new GetCurrencyResponseData(tokenData));
                }
            }
        });
        tokenDetails.sort(Comparator.comparing(GetCurrencyResponseData::getName));
        return ResponseEntity.status(HttpStatus.OK).body(new GetCurrenciesResponse(new GetCurrencyResponseData(nativeCurrencyData), tokenDetails));
    }

}
