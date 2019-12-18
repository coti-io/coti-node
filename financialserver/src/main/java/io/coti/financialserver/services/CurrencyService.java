package io.coti.financialserver.services;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.coti.basenode.crypto.CurrencyOriginatorCrypto;
import io.coti.basenode.crypto.CurrencyRegistrarCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.exceptions.CurrencyValidationException;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.IMintingService;
import io.coti.financialserver.crypto.GenerateTokenRequestCrypto;
import io.coti.financialserver.crypto.GetUserTokensRequestCrypto;
import io.coti.financialserver.data.CurrencyNameIndexData;
import io.coti.financialserver.http.*;
import io.coti.financialserver.http.data.CurrencyDataForFee;
import io.coti.financialserver.http.data.GeneratedTokenResponseData;
import io.coti.financialserver.http.data.GetCurrencyResponseData;
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

    public static final String GENERATED_TOKEN_ENDPOINT = "/currenciesCollection/token";
    private static final String GET_NATIVE_CURRENCY_ENDPOINT = "/currenciesCollection/native";
    public static final String S_EXCEPTION_S = "%s. Exception: %s";
    private final Map<Hash, Hash> lockUserHashMap = new ConcurrentHashMap<>();
    private final Map<Hash, Hash> lockTransactionHashMap = new ConcurrentHashMap<>();
    private final Set<String> processingCurrencyNameSet = Sets.newConcurrentHashSet();
    private final Set<String> processingCurrencySymbolSet = Sets.newConcurrentHashSet();
    @Value("${currency.genesis.address}")
    private Hash currencyGenesisAddress;
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
    private CurrencyRegistrarCrypto currencyRegistrarCrypto;
    @Autowired
    private Transactions transactions;
    @Autowired
    private Currencies currenciesCollection;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private IClusterStampService clusterStampService;
    @Autowired
    private FeeService feeService;
    @Autowired
    private IMintingService mintingService;
    @Autowired
    private IBalanceService iBalanceService;

    private BlockingQueue<TransactionData> pendingCurrencyTransactionQueue;
    private BlockingQueue<TransactionData> tokenGenerationTransactionQueue;
    private Thread pendingCurrencyTransactionThread;
    private Thread tokenGenerationTransactionThread;

    @Override
    public void init() {
        super.init();
        initQueuesAndThreads();
    }

    private void initQueuesAndThreads() {
        pendingCurrencyTransactionQueue = new LinkedBlockingQueue<>();
        tokenGenerationTransactionQueue = new LinkedBlockingQueue<>();
        pendingCurrencyTransactionThread = new Thread(this::handlePendingCurrencies);
        pendingCurrencyTransactionThread.start();
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

    public void addToPendingCurrencyTransactionQueue(TransactionData transactionData) {
        addToTransactionQueue(pendingCurrencyTransactionQueue, transactionData);
    }

    public void addToTokenGenerationTransactionQueue(TransactionData transactionData) {
        addToTransactionQueue(tokenGenerationTransactionQueue, transactionData);
    }

    private void updateCurrencyNameIndex(CurrencyData currencyData) {
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
        updateCurrencyNameIndex(currencyData);
    }


    private void putPendingCurrencyData(CurrencyData currencyData) {
        if (currencyData == null) {
            throw new CurrencyException("Failed to add an empty currency");
        }
        pendingCurrencies.put(currencyData);
        updateCurrencyHashByTypeMap(currencyData);
        updateCurrencyNameIndex(currencyData);
    }

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
        CurrencyData pendingCurrencyData = pendingCurrencies.getByHash(currencyHash);
        if (pendingCurrencyData != null) {
            generatedTokens.add(new GeneratedTokenResponseData(transactionHash, pendingCurrencyData, false));
            return;
        }
        CurrencyData currencyData = currenciesCollection.getByHash(currencyHash);
        if (currencyData == null) {
            throw new CurrencyException(String.format("Unidentified currency hash: %s", currencyHash));
        }
        GeneratedTokenResponseData generatedTokenResponseData = new GeneratedTokenResponseData(transactionHash, currencyData, true);

        BigDecimal genesisAddressBalance = iBalanceService.getBalance(currencyGenesisAddress, currencyHash);
        BigDecimal mintedAmount = BigDecimal.ZERO;
        if (genesisAddressBalance != null) {
            mintedAmount = currencyData.getTotalSupply().subtract(genesisAddressBalance);
        }
        generatedTokenResponseData.getToken().setMintedAmount(mintedAmount);
        generatedTokenResponseData.getToken().setRequestedMintingAmount(mintingService.getTokenAllocatedAmount(currencyHash).subtract(mintedAmount));
        generatedTokens.add(generatedTokenResponseData);
    }

    public ResponseEntity<IResponse> generateToken(GenerateTokenRequest generateTokenRequest) {
        try {
            validateTokenGenerationRequest(generateTokenRequest);
            occupyLocksToProcessingSets(generateTokenRequest);
            Hash tokenHash = validateUniquenessAndAddToken(generateTokenRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(new GenerateTokenResponse(tokenHash));
        } catch (CurrencyValidationException e) {
            String error = String.format(S_EXCEPTION_S, TOKEN_GENERATION_REQUEST_FAILURE, e.getMessageAndCause());
            return ResponseEntity.badRequest().body(new Response(error, STATUS_ERROR));
        } catch (CotiRunTimeException e) {
            String error = String.format(S_EXCEPTION_S, TOKEN_GENERATION_REQUEST_FAILURE, e.getMessageAndCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        } catch (Exception e) {
            String error = String.format(S_EXCEPTION_S, TOKEN_GENERATION_REQUEST_FAILURE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        } finally {
            removeOccupyLocksFromProcessingSets(generateTokenRequest);
        }
    }

    public ResponseEntity<IResponse> getTokenGenerationFee(GenerateTokenFeeRequest generateTokenRequest) {
        Hash currencyHash;
        try {
            CurrencyDataForFee requestCurrencyData = generateTokenRequest.getCurrencyData();
            String currencyName = requestCurrencyData.getName();
            currencyHash = requestCurrencyData.calculateHash();
            validateCurrencyUniqueness(currencyHash, currencyName);
        } catch (CurrencyValidationException e) {
            String error = String.format(S_EXCEPTION_S, TOKEN_GENERATION_FEE_FAILURE, e.getMessageAndCause());
            return ResponseEntity.badRequest().body(new Response(error, STATUS_ERROR));
        } catch (CotiRunTimeException e) {
            String error = String.format(S_EXCEPTION_S, TOKEN_GENERATION_FEE_FAILURE, e.getMessageAndCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        } catch (Exception e) {
            String error = String.format(S_EXCEPTION_S, TOKEN_GENERATION_FEE_FAILURE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        }

        return feeService.createTokenGenerationFee(generateTokenRequest, currencyHash);
    }

    private void removeOccupyLocksFromProcessingSets(GenerateTokenRequest generateTokenRequest) {
        removeLockFromProcessingSet(processingCurrencySymbolSet, generateTokenRequest.getOriginatorCurrencyData().getSymbol());
        removeLockFromProcessingSet(processingCurrencyNameSet, generateTokenRequest.getOriginatorCurrencyData().getName());
    }

    private void occupyLocksToProcessingSets(GenerateTokenRequest generateTokenRequest) {
        addLockToProcessingSet(processingCurrencyNameSet, generateTokenRequest.getOriginatorCurrencyData().getName());
        addLockToProcessingSet(processingCurrencySymbolSet, generateTokenRequest.getOriginatorCurrencyData().getSymbol());
    }

    private Hash validateUniquenessAndAddToken(GenerateTokenRequest generateTokenRequest) {
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

            validateTransactionAvailability(userTokenGenerationData, requestTransactionHash);
            validateTransactionAmount(originatorCurrencyData, requestTransactionHash);
            validateCurrencyUniqueness(currencyHash, currencyName);

            CurrencyType currencyType = CurrencyType.REGULAR_CMD_TOKEN;
            CurrencyTypeData currencyTypeData = new CurrencyTypeData(currencyType, Instant.now());
            currencyData = new CurrencyData(originatorCurrencyData, currencyTypeData);
            setSignedCurrencyTypeData(currencyData, currencyType);
            currencyRegistrarCrypto.signMessage(currencyData);

            synchronized (addLockToLockMap(lockTransactionHashMap, requestTransactionHash)) {
                TransactionData tokenGenerationTransactionData = transactions.getByHash(requestTransactionHash);
                Hash requestCurrencyDataHash = currencyData.getHash();
                userTokenGenerationData.getTransactionHashToCurrencyMap().put(requestTransactionHash, requestCurrencyDataHash);
                if (transactionHelper.isConfirmed(tokenGenerationTransactionData)) {
                    putCurrencyData(currencyData);
                    tokenConfirmed = true;
                } else {
                    putPendingCurrencyData(currencyData);
                }
                removeLockFromLocksMap(lockTransactionHashMap, requestTransactionHash);
            }
            userTokenGenerations.put(userTokenGenerationData);
            removeLockFromLocksMap(lockUserHashMap, generateTokenRequest.getSignerHash());
        }
        if (tokenConfirmed) {
            sendGeneratedToken(currencyData);
        }

        return currencyData.getHash();
    }

    private void sendGeneratedToken(CurrencyData currencyData) {
        try {
            String initiatorAddress = networkService.getSingleNodeData(NodeType.ZeroSpendServer).getHttpFullAddress();
            restTemplate.postForObject(initiatorAddress + GENERATED_TOKEN_ENDPOINT, currencyData, Response.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new CurrencyException(String.format("Error at sending generated token. Initiator server response: %s", new Gson().fromJson(e.getResponseBodyAsString(), Response.class).getMessage()));
        } catch (Exception e) {
            throw new CurrencyException("Error at sending generated token.", e);
        }
    }


    protected void setSignedCurrencyTypeData(CurrencyData currencyData, CurrencyType currencyType) {
        CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(currencyData.getHash(), currencyType, Instant.now());
        currencyTypeRegistrationCrypto.signMessage(currencyTypeRegistrationData);
        currencyData.getCurrencyTypeData().setRegistrarSignature(currencyTypeRegistrationData.getRegistrarSignature());
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

    private void validateTransactionAvailability(UserTokenGenerationData userTokenGenerationData, Hash requestTransactionHash) {
        final Hash existingCurrencyHash = userTokenGenerationData.getTransactionHashToCurrencyMap().get(requestTransactionHash);
        if (existingCurrencyHash != null) {
            throw new CurrencyException(String.format("Transaction hash %s was already used", requestTransactionHash));
        }
    }

    private void validateTransactionAmount(OriginatorCurrencyData requestCurrencyData, Hash requestTransactionHash) {
        TransactionData tokenGenerationTransactionData = transactions.getByHash(requestTransactionHash);
        Optional<BaseTransactionData> first = tokenGenerationTransactionData.getBaseTransactions().stream()
                .filter(baseTransactionData -> baseTransactionData instanceof TokenFeeBaseTransactionData).findFirst();

        BaseTransactionData tokenServiceFeeData = null;
        if (first.isPresent()) {
            tokenServiceFeeData = first.get();
        }

        if (tokenServiceFeeData == null || !tokenServiceFeeData.getAmount().equals(feeService.calculateTokenGenerationFee(requestCurrencyData.getTotalSupply()))) {
            throw new CurrencyException(String.format("The token generation fees in the transaction %s is not correct", requestTransactionHash));
        }
    }

    private void validateCurrencyUniqueness(Hash currencyHash, String currencyName) {
        if (currencyNameIndexes.getByHash(new CurrencyNameIndexData(currencyName, currencyHash).getHash()) != null) {
            throw new CurrencyException("Currency name is already in use.");
        }
        if (pendingCurrencies.getByHash(currencyHash) != null || currenciesCollection.getByHash(currencyHash) != null) {
            throw new CurrencyException("Currency symbol is already in use.");
        }
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

    private void addLockToProcessingSet(Set<String> lockProcessingSet, String lock) {
        synchronized (lockProcessingSet) {
            if (lockProcessingSet.contains(lock)) {
                throw new CurrencyException(String.format("%s is in progress", lock));
            } else {
                lockProcessingSet.add(lock);
            }
        }
    }

    private void removeLockFromProcessingSet(Set<String> lockProcessingSet, String lock) {
        synchronized (lockProcessingSet) {
            lockProcessingSet.remove(lock);
        }
    }

    private void handlePendingCurrencies() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TransactionData transactionData = pendingCurrencyTransactionQueue.take();
                final Hash userHash = transactionData.getSenderHash();
                Hash transactionHash = transactionData.getHash();
                UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(userHash);
                if (userTokenGenerationData != null) {
                    final Hash currencyHash = userTokenGenerations.getByHash(userHash).getTransactionHashToCurrencyMap().get(transactionHash);
                    if (currencyHash != null) {
                        synchronized (addLockToLockMap(lockTransactionHashMap, transactionHash)) {
                            CurrencyData pendingCurrency = pendingCurrencies.getByHash(currencyHash);
                            if (pendingCurrency != null) {
                                pendingCurrencies.deleteByHash(currencyHash);
                                putCurrencyData(pendingCurrency);
                                sendGeneratedToken(pendingCurrency);
                            }
                            removeLockFromLocksMap(lockTransactionHashMap, transactionHash);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handlePropagatedTokenGenerationTransactions() {
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

    @Override
    public void handleInitiatedTokenNotice(InitiatedTokenNoticeData initiatedTokenNoticeData) {
        CurrencyData currencyData = initiatedTokenNoticeData.getCurrencyData();
        if (!verifyCurrencyExists(currencyData.getHash())) {
            log.error("Propagated currency {} does not exist", currencyData.getName());
            return;
        }
        clusterStampService.handleInitiatedTokenNotice(initiatedTokenNoticeData);
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
