package io.coti.financialserver.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.CurrencyValidationException;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Currencies;
import io.coti.financialserver.crypto.*;
import io.coti.financialserver.data.MintedTokenData;
import io.coti.financialserver.data.MintingFeeWarrantData;
import io.coti.financialserver.data.MintingRecordData;
import io.coti.financialserver.data.ReservedAddress;
import io.coti.financialserver.http.*;
import io.coti.financialserver.http.data.*;
import io.coti.financialserver.model.MintingRecords;
import io.coti.financialserver.model.UserTokenGenerations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class MintingService {

    @Value("${financialserver.seed}")
    private String seed;

    private Map<Hash, Hash> lockMintingRecordHashMap = new ConcurrentHashMap<>();

    @Autowired
    private MintingTokenFeeRequestCrypto mintingTokenFeeRequestCrypto;
    @Autowired
    private UserTokenGenerations userTokenGenerations;
    @Autowired
    private Currencies currencies;
    @Autowired
    private FeeService feeService;
    @Autowired
    private MintingRecords mintingRecords;
    @Autowired
    private CurrencyService currencyService;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private GetMintingQuotesRequestCrypto getMintingQuotesRequestCrypto;
    @Autowired
    private GetMintingHistoryRequestCrypto getMintingHistoryRequestCrypto;
    @Autowired
    private DeleteTokenMintingQuoteRequestCrypto deleteTokenMintingQuoteRequestCrypto;
    @Autowired
    private GetTokenMintingFeeQuoteRequestCrypto getTokenMintingFeeQuoteRequestCrypto;
    @Autowired
    private MintingFeeWarrantCrypto mintingFeeWarrantCrypto;

    private BlockingQueue<TransactionData> propagatedTokenMintingFeeTransactionQueue;
    private Thread propagatedTokenMintingFeeTransactionThread;
    private BlockingQueue<TransactionData> confirmedTokenMintingFeeTransactionQueue;
    private Thread confirmedTokenMintingFeeTransactionThread;

    @PostConstruct
    public void init() {
        initQueuesAndThreads();
    }

    private void initQueuesAndThreads() {
        propagatedTokenMintingFeeTransactionQueue = new LinkedBlockingQueue<>();
        propagatedTokenMintingFeeTransactionThread = new Thread(this::handlePropagatedTokenMintingTransactions);
        propagatedTokenMintingFeeTransactionThread.start();
        confirmedTokenMintingFeeTransactionQueue = new LinkedBlockingQueue<>();
        confirmedTokenMintingFeeTransactionThread = new Thread(this::handleConfirmedTokenMintingTransactions);
        confirmedTokenMintingFeeTransactionThread.start();
    }

    private void addToTransactionQueue(BlockingQueue<TransactionData> queue, TransactionData transactionData) {
        try {
            queue.put(transactionData);
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for insertion of transaction {} into blocking queue.", transactionData.getHash());
            Thread.currentThread().interrupt();
        }
    }

    public void addToPropagatedTokenMintingFeeTransactionQueue(TransactionData transactionData) {
        addToTransactionQueue(propagatedTokenMintingFeeTransactionQueue, transactionData);
    }

    public void addToConfirmedTokenMintingFeeTransactionQueue(TransactionData transactionData) {
        addToTransactionQueue(confirmedTokenMintingFeeTransactionQueue, transactionData);
    }

    public ResponseEntity<IResponse> getTokenMintingFee(MintingTokenFeeRequest mintingTokenFeeRequest) {
        CurrencyData currencyData;
        try {
            currencyData = validateMintingTokenFeeRequestAndGetCurrencyData(mintingTokenFeeRequest);
            if (!validateTokenSupplyAvailableAndWarrantAmount(mintingTokenFeeRequest, currencyData)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_AMOUNT, STATUS_ERROR));
            }
            if (!isAddressValid(mintingTokenFeeRequest.getReceiverAddress())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_ADDRESS, STATUS_ERROR));
            }
        } catch (CurrencyValidationException e) {
            String error = String.format("%s. Exception: %s", TOKEN_MINTING_FEE_FAILURE, e.getMessageAndCause());
            return ResponseEntity.badRequest().body(new Response(error, STATUS_ERROR));
        } catch (CotiRunTimeException e) {
            String error = String.format("%s. Exception: %s", TOKEN_MINTING_FEE_FAILURE, e.getMessageAndCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        } catch (Exception e) {
            String error = String.format("%s. Exception: %s", TOKEN_MINTING_FEE_FAILURE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        }
        return createTokenMintingFee(mintingTokenFeeRequest, currencyData);
    }

    public ResponseEntity<IResponse> getTokenMintingFeeRetry(MintingTokenFeeRequest mintingTokenFeeRequest) {
        CurrencyData currencyData;
        MintingFeeWarrantData mintingFeeWarrantData;
        try {
            if (mintingTokenFeeRequest.getMintingFeeWarrantData() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_WARRANT, STATUS_ERROR));
            }
            currencyData = validateMintingTokenFeeRequestAndGetCurrencyData(mintingTokenFeeRequest);
            if (!validateTokenSupplyAvailableAndWarrantAmount(mintingTokenFeeRequest, currencyData)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_AMOUNT, STATUS_ERROR));
            }
            if (!isAddressValid(mintingTokenFeeRequest.getReceiverAddress())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_ADDRESS, STATUS_ERROR));
            }
            mintingFeeWarrantData = mintingRecords.getByHash(currencyData.getHash())
                    .getMintingFeeWarrants().get(mintingTokenFeeRequest.getMintingFeeWarrantData().getHash());

            if (!mintingFeeWarrantData.isStillValid(Instant.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_WARRANT, STATUS_ERROR));
            }

            MintingRecordData mintingRecordData = mintingRecords.getByHash(currencyData.getHash());
            for (MintingRequestData mintingRequestData : mintingRecordData.getMintingRequests().values()) {
                if (mintingRequestData.getMintingFeeWarrantHash().equals(mintingTokenFeeRequest.getMintingFeeWarrantData().getHash())
                        && mintingRequestData.isStillValid(Instant.now()) && !mintingFeeWarrantData.isRunningAlready()) {
                    return createTokenMintingFee(mintingRequestData);
                }
            }
        } catch (CurrencyValidationException e) {
            String error = String.format("%s. Exception: %s", TOKEN_MINTING_FEE_FAILURE, e.getMessageAndCause());
            return ResponseEntity.badRequest().body(new Response(error, STATUS_ERROR));
        } catch (CotiRunTimeException e) {
            String error = String.format("%s. Exception: %s", TOKEN_MINTING_FEE_FAILURE, e.getMessageAndCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        } catch (Exception e) {
            String error = String.format("%s. Exception: %s", TOKEN_MINTING_FEE_FAILURE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        }

        return createTokenMintingFee(mintingTokenFeeRequest, currencyData, mintingFeeWarrantData);
    }

    public ResponseEntity<IResponse> createTokenMintingFee(MintingTokenFeeRequest mintingTokenFeeRequest, CurrencyData currencyData) {
        try {
            BigDecimal mintingFee = null;
            MintingFeeWarrantData mintingFeeWarrantData = mintingTokenFeeRequest.getMintingFeeWarrantData();
            if (mintingTokenFeeRequest.getMintingFeeWarrantData() != null
                    && mintingFeeWarrantData != null && mintingFeeWarrantData.isStillValid(Instant.now())) {
                Hash feeWarrantHashFromRequest = mintingTokenFeeRequest.getMintingFeeWarrantData().getHash();
                if (!mintingFeeWarrantCrypto.verifySignature(mintingFeeWarrantData)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
                }
                if (!feeWarrantHashFromRequest.equals(mintingFeeWarrantData.getHash())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_WARRANT_MISMATCHED, STATUS_ERROR));
                }
                mintingFee = mintingFeeWarrantData.getFeeGiven();
            }
            if (mintingFee == null) {
                mintingFee = feeService.calculateTokenMintingFee(mintingTokenFeeRequest.getMintingFeeData().getAmount(),
                        Instant.now(), currencyData);
            }
            return createTokenMintingFeeAndMintingRequest(mintingTokenFeeRequest, currencyData, mintingFee);
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    public ResponseEntity<IResponse> createTokenMintingFee(MintingTokenFeeRequest mintingTokenFeeRequest, CurrencyData currencyData,
                                                           MintingFeeWarrantData mintingFeeWarrantData) {
        try {
            return createTokenMintingFeeAndMintingRequest(mintingTokenFeeRequest, currencyData, mintingFeeWarrantData.getFeeGiven());
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    public ResponseEntity<IResponse> createTokenMintingFeeAndMintingRequest(MintingTokenFeeRequest mintingTokenFeeRequest, CurrencyData currencyData, BigDecimal mintingFee)
            throws ClassNotFoundException {
        TokenServiceFeeData tokenServiceFeeData = new TokenServiceFeeData(feeService.networkFeeAddress(), currencyService.getNativeCurrencyHash(),
                NodeCryptoHelper.getNodeHash(), mintingFee, currencyData.getHash(), mintingTokenFeeRequest.getMintingFeeData().getAmount(), Instant.now());
        feeService.setFeeHash(tokenServiceFeeData);
        feeService.signTokenGenerationFee(tokenServiceFeeData);
        TokenServiceFeeResponseData tokenServiceFeeResponseData = new TokenServiceFeeResponseData(tokenServiceFeeData);
        Hash feeWarrantHashFromRequest = (mintingTokenFeeRequest.getMintingFeeWarrantData() == null) ? null : mintingTokenFeeRequest.getMintingFeeWarrantData().getHash();
        MintingRequestData mintingRequestData = new MintingRequestData(feeWarrantHashFromRequest, Instant.now(),
                mintingTokenFeeRequest.getMintingFeeData().getAmount(), mintingTokenFeeRequest.getReceiverAddress(), tokenServiceFeeData);

        synchronized (addLockToLockMap(lockMintingRecordHashMap, mintingTokenFeeRequest.getMintingFeeData().getCurrencyHash())) {
            MintingRecordData mintingRecordData = mintingRecords.getByHash(mintingTokenFeeRequest.getMintingFeeData().getCurrencyHash());
            mintingRecordData.getMintingRequests().put(tokenServiceFeeData.getHash(), mintingRequestData);
            if (feeWarrantHashFromRequest != null) {
                if (mintingRecordData.getMintingFeeWarrants().containsKey(feeWarrantHashFromRequest)) {
                    removeLockFromLocksMap(lockMintingRecordHashMap, mintingTokenFeeRequest.getMintingFeeData().getCurrencyHash());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_WARRANT_USED, STATUS_ERROR));
                }
                mintingRecordData.getMintingFeeWarrants().putIfAbsent(feeWarrantHashFromRequest, mintingTokenFeeRequest.getMintingFeeWarrantData());

            }
            mintingRecordData.setTotalRequestedSupply(mintingRecordData.getTotalRequestedSupply().add(mintingTokenFeeRequest.getMintingFeeData().getAmount()));
            mintingRecords.put(mintingRecordData);
            removeLockFromLocksMap(lockMintingRecordHashMap, mintingTokenFeeRequest.getMintingFeeData().getCurrencyHash());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenFeeResponse(tokenServiceFeeResponseData));
    }

    public ResponseEntity<IResponse> createTokenMintingFee(MintingRequestData mintingRequestData) {
        TokenServiceFeeResponseData tokenServiceFeeResponseData = new TokenServiceFeeResponseData(mintingRequestData.getTokenServiceFeeData());
        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenFeeResponse(tokenServiceFeeResponseData));
    }

    private CurrencyData validateMintingTokenFeeRequestAndGetCurrencyData(MintingTokenFeeRequest mintingTokenFeeRequest) {
        if (!mintingTokenFeeRequestCrypto.verifySignature(mintingTokenFeeRequest)) {
            throw new CurrencyValidationException(TOKEN_MINTING_REQUEST_INVALID_SIGNATURE);
        }
        CurrencyData currencyData = currencies.getByHash(mintingTokenFeeRequest.getMintingFeeData().getCurrencyHash());
        if (currencyData == null) {
            throw new CurrencyValidationException(TOKEN_MINTING_REQUEST_INVALID_CURRENCY);
        }
        if (!currencyData.getOriginatorHash().equals(mintingTokenFeeRequest.getUserHash())) {
            throw new CurrencyValidationException(TOKEN_MINTING_REQUEST_INVALID_ORIGINATOR);
        }
        return currencyData;
    }

    private boolean isAddressValid(Hash address) {
        return CryptoHelper.isAddressValid(address);
    }

    private boolean validateTokenSupplyAvailableAndWarrantAmount(MintingTokenFeeRequest mintingTokenFeeRequest, CurrencyData currencyData) {
        MintingRecordData mintingRecordData;

        synchronized (addLockToLockMap(lockMintingRecordHashMap, currencyData.getHash())) {
            mintingRecordData = mintingRecords.getByHash(currencyData.getHash());
            if (mintingRecordData == null) {
                mintingRecordData = new MintingRecordData(currencyData.getHash());
                mintingRecords.put(mintingRecordData);
            }
            removeLockFromLocksMap(lockMintingRecordHashMap, currencyData.getHash());
        }

        if (mintingTokenFeeRequest.getMintingFeeWarrantData() != null) {
            MintingFeeWarrantData mintingFeeWarrantData = mintingTokenFeeRequest.getMintingFeeWarrantData();
            if (!mintingFeeWarrantData.getAmount().equals(mintingTokenFeeRequest.getMintingFeeData().getAmount())
                    || !mintingFeeWarrantData.getCurrencyHash().equals(mintingTokenFeeRequest.getMintingFeeData().getCurrencyHash())) {
                return false;
            }
        }

        BigDecimal tokenSupplyAvailable = currencyData.getTotalSupply().subtract(mintingRecordData.getTokenMintedAmount()).subtract(mintingRecordData.getTotalRequestedSupply());
        return mintingTokenFeeRequest.getMintingFeeData().getAmount().compareTo(tokenSupplyAvailable) != 1;
    }

    public ResponseEntity<IResponse> getTokenMintingQuotes(GetMintingQuotesRequest getMintingQuotesRequest) {
        try {
            if (!getMintingQuotesRequestCrypto.verifySignature(getMintingQuotesRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            Hash userHash = getMintingQuotesRequest.getUserHash();
            HashSet<Hash> userTokenHashes = getUserTokenHashes(userHash);
            Map<String, GetMintingTokenQuoteData> getMintingQuotesResponseData = new HashMap<>();
            if (userTokenHashes.isEmpty()) {
                return ResponseEntity.ok(new GetMintingQuotesResponse(getMintingQuotesResponseData));
            }

            userTokenHashes.forEach(currencyHash -> fillValidMintingQuotes(currencyHash, getMintingQuotesResponseData));
            return ResponseEntity.ok(new GetMintingQuotesResponse(getMintingQuotesResponseData));

        } catch (Exception e) {
            log.error("Error at getting user minting tokens quotes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private HashSet<Hash> getUserTokenHashes(Hash userHash) {
        HashSet<Hash> userTokenHashes = new HashSet<>();
        UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(userHash);
        if (userTokenGenerationData != null) {
            Collection<Hash> currencyHashes = userTokenGenerationData.getTransactionHashToCurrencyMap().values();
            currencyHashes.forEach(currencyHash -> fillUserTokenHashes(userTokenHashes, currencyHash));
        }
        return userTokenHashes;
    }

    private void fillUserTokenHashes(HashSet<Hash> userTokenHashes, Hash currencyHash) {
        if (currencyHash != null) {
            CurrencyData currencyData = currencies.getByHash(currencyHash);
            if (currencyData != null) {
                userTokenHashes.add(currencyData.getHash());
            }
        }
    }

    private void fillValidMintingQuotes(Hash currencyHash, Map<String, GetMintingTokenQuoteData> getMintingQuotesResponseData) {
        HashMap<Hash, Hash> mintingFeeWarrantToMintingRequestMap = fillMintingFeeWarrantToMintingRequest(currencyHash);
        synchronized (addLockToLockMap(lockMintingRecordHashMap, currencyHash)) {
            MintingRecordData mintingRecordData = mintingRecords.getByHash(currencyHash);
            if (mintingRecordData != null) {
                HashMap<Hash, MintingFeeWarrantData> mintingFeeWarrants = mintingRecordData.getMintingFeeWarrants();
                if (mintingFeeWarrants != null && !mintingFeeWarrants.isEmpty()) {
                    mintingFeeWarrants.entrySet().removeIf(entry -> !entry.getValue().isStillValid(Instant.now()));
                    mintingFeeWarrants.entrySet().forEach(mintingFeeWarrant -> {
                        fillValidMintingTokenQuote(currencyHash, getMintingQuotesResponseData, mintingFeeWarrantToMintingRequestMap, mintingRecordData, mintingFeeWarrant);
                    });
                }
                mintingRecords.put(mintingRecordData);
            }
            removeLockFromLocksMap(lockMintingRecordHashMap, currencyHash);
        }
    }


    private void fillValidMintingTokenQuote(Hash currencyHash, Map<String, GetMintingTokenQuoteData> getMintingQuotesResponseData, HashMap<Hash, Hash> mintingFeeWarrantToMintingRequestMap, MintingRecordData mintingRecordData, Map.Entry<Hash, MintingFeeWarrantData> mintingFeeWarrant) {
        MintingFeeWarrantData mintingFeeWarrantData = mintingFeeWarrant.getValue();
        HashMap<Hash, MintingRequestData> mintingRequests = mintingRecordData.getMintingRequests();
        if (getMintingQuotesResponseData.get(currencyHash.toString()) == null) {
            getMintingQuotesResponseData.put(currencyHash.toString(),
                    new GetMintingTokenQuoteData(new HashMap<>(), mintingRecordData.getTotalRequestedSupply(), mintingRecordData.getTokenMintedAmount()));
        }
        MintingResponseData mintingResponseData = null;
        if (mintingRequests != null && !mintingRequests.isEmpty()) {
            mintingResponseData = new MintingResponseData(mintingRequests.get(mintingFeeWarrantToMintingRequestMap.get(mintingFeeWarrant.getKey())));
        }
        getMintingQuotesResponseData.get(currencyHash.toString()).getMintingFeeQuotes()
                .put(mintingFeeWarrantData.getHash().toString(), new GetMintingQuoteResponseData(mintingFeeWarrantData, mintingResponseData));
    }

    private HashMap<Hash, Hash> fillMintingFeeWarrantToMintingRequest(Hash currencyHash) {
        HashMap<Hash, Hash> mintingFeeWarrantToMintingRequestMap = new HashMap<>();
        synchronized (addLockToLockMap(lockMintingRecordHashMap, currencyHash)) {
            MintingRecordData mintingRecordData = mintingRecords.getByHash(currencyHash);
            if (mintingRecordData != null) {
                HashMap<Hash, MintingRequestData> mintingRequests = mintingRecordData.getMintingRequests();
                if (mintingRequests != null && !mintingRequests.isEmpty()) {
                    mintingRequests.entrySet().removeIf(entry -> !entry.getValue().isStillValid(Instant.now()));
                    mintingRequests.entrySet().forEach(entry -> {
                        mintingFeeWarrantToMintingRequestMap.put(entry.getValue().getMintingFeeWarrantHash(), entry.getValue().getTokenServiceFeeData().getHash());
                    });
                    mintingRecords.put(mintingRecordData);
                }
            }
            removeLockFromLocksMap(lockMintingRecordHashMap, currencyHash);
        }
        return mintingFeeWarrantToMintingRequestMap;
    }


    public ResponseEntity<IResponse> getTokenMintingHistory(GetMintingHistoryRequest getMintingHistoryRequest) {
        try {
            if (!getMintingHistoryRequestCrypto.verifySignature(getMintingHistoryRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            HashSet<Hash> userTokenHashes = getUserTokenHashes(getMintingHistoryRequest.getUserHash());

            Map<Hash, Map<Instant, MintedTokenData>> mintingHistory = new HashMap<>();
            if (userTokenHashes.isEmpty()) {
                return ResponseEntity.ok(new GetMintingHistoryResponse(mintingHistory));
            }
            userTokenHashes.forEach(currencyHash -> fillMintingHistory(currencyHash, mintingHistory));
            return ResponseEntity.ok(new GetMintingHistoryResponse(mintingHistory));
        } catch (Exception e) {
            log.error("Error at getting user minting history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private void fillMintingHistory(Hash currencyHash, Map<Hash, Map<Instant, MintedTokenData>> mintingHistory) {
        synchronized (addLockToLockMap(lockMintingRecordHashMap, currencyHash)) {
            MintingRecordData mintingRecordData = mintingRecords.getByHash(currencyHash);
            if (mintingRecordData != null) {
                Map<Instant, MintedTokenData> mintingTokenHistory = mintingRecordData.getMintingHistory();
                mintingHistory.put(currencyHash, mintingTokenHistory);
                mintingRecords.put(mintingRecordData);
            }
            removeLockFromLocksMap(lockMintingRecordHashMap, currencyHash);
        }
    }

    private void handlePropagatedTokenMintingTransactions() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TransactionData tokenMintingTransaction = propagatedTokenMintingFeeTransactionQueue.take();
                TokenServiceFeeData tokenServiceFeeData = getTokenServiceFeeData(tokenMintingTransaction);
                if (tokenServiceFeeData != null) {
                    synchronized (addLockToLockMap(lockMintingRecordHashMap, tokenServiceFeeData.getTokenHash())) {
                        MintingRecordData mintingRecordData = mintingRecords.getByHash(tokenServiceFeeData.getTokenHash());
                        if (mintingRecordData != null) {
                            MintingRequestData mintingRequestData = mintingRecordData.getMintingRequests().get(tokenServiceFeeData.getHash());
                            if (mintingRequestData != null) {
                                if (mintingRequestData.getMintingFeeWarrantHash() != null) {
                                    MintingFeeWarrantData mintingFeeWarrantData = mintingRecordData.getMintingFeeWarrants().get(mintingRequestData.getMintingFeeWarrantHash());
                                    if (mintingFeeWarrantData != null) {
                                        mintingFeeWarrantData.setRunningAlready(true);
                                        mintingRecords.put(mintingRecordData);
                                    }
                                }
                            } else {
                                log.error("TokenMinting transaction {} without minting request", tokenMintingTransaction.getHash());
                            }
                        } else {
                            log.error("TokenMinting transaction {} without minting record", tokenMintingTransaction.getHash());
                        }
                        removeLockFromLocksMap(lockMintingRecordHashMap, tokenServiceFeeData.getTokenHash());
                    }
                } else {
                    log.error("TokenMinting transaction {} without TFBT", tokenMintingTransaction.getHash());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private TokenServiceFeeData getTokenServiceFeeData(TransactionData tokenMintingTransaction) {
        return (TokenServiceFeeData) tokenMintingTransaction
                .getBaseTransactions()
                .stream()
                .filter(t -> t instanceof TokenServiceFeeData)
                .findFirst().orElse(null);
    }

    private void handleConfirmedTokenMintingTransactions() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TransactionData tokenMintingTransaction = confirmedTokenMintingFeeTransactionQueue.take();
                TokenServiceFeeData tokenServiceFeeData = getTokenServiceFeeData(tokenMintingTransaction);
                if (tokenServiceFeeData == null) {
                    log.error("TokenMinting transaction {} without TFBT", tokenMintingTransaction.getHash());
                    continue;
                }

                synchronized (addLockToLockMap(lockMintingRecordHashMap, tokenServiceFeeData.getTokenHash())) {
                    MintingRecordData mintingRecordData = mintingRecords.getByHash(tokenServiceFeeData.getTokenHash());
                    if (mintingRecordData == null) {
                        log.error("TokenMinting transaction {} without minting record", tokenMintingTransaction.getHash());
                        continue;
                    }

                    MintingRequestData mintingRequestData = mintingRecordData.getMintingRequests().get(tokenServiceFeeData.getHash());
                    if (mintingRequestData == null) {
                        log.error("TokenMinting transaction {} without minting request", tokenMintingTransaction.getHash());
                        continue;
                    }

                    int genesisAddressIndex = Math.toIntExact(ReservedAddress.GENESIS_ONE.getIndex());
                    Hash cotiGenesisAddress = nodeCryptoHelper.generateAddress(seed, genesisAddressIndex);
                    Hash initialTransactionHash = transactionCreationService.createInitialTransaction(mintingRequestData.getAmount(), tokenServiceFeeData.getTokenHash(),
                            cotiGenesisAddress, mintingRequestData.getReceiverAddress(), genesisAddressIndex);

                    if (initialTransactionHash != null) {
                        mintingRecordData.getMintingFeeWarrants().remove(mintingRequestData.getMintingFeeWarrantHash());
                        mintingRecordData.getMintingRequests().remove(tokenServiceFeeData.getHash());
                        MintedTokenData mintedTokenData = new MintedTokenData(tokenServiceFeeData.getTokenHash(), Instant.now(),
                                mintingRequestData.getAmount(), initialTransactionHash, tokenMintingTransaction.getHash());
                        mintingRecordData.getMintingHistory().put(mintedTokenData.getMintingTime(), mintedTokenData);
                        mintingRecordData.setTokenMintedAmount(mintingRecordData.getTokenMintedAmount().add(mintingRequestData.getAmount()));
                        mintingRecordData.setTotalRequestedSupply(mintingRecordData.getTotalRequestedSupply().subtract(mintingRequestData.getAmount()));
                        mintingRecords.put(mintingRecordData);
                    }
                    removeLockFromLocksMap(lockMintingRecordHashMap, tokenServiceFeeData.getTokenHash());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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

    public ResponseEntity<IResponse> deleteTokenMintingQuote(DeleteTokenMintingQuoteRequest
                                                                     deleteTokenMintingQuoteRequest) {
        try {
            if (!deleteTokenMintingQuoteRequestCrypto.verifySignature(deleteTokenMintingQuoteRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            Hash userHash = deleteTokenMintingQuoteRequest.getUserHash();
            Hash warrantFeeHash = deleteTokenMintingQuoteRequest.getWarrantFeeHash();
            Hash currencyHash = deleteTokenMintingQuoteRequest.getCurrencyHash();

            MintingFeeWarrantData removedMintingFeeWarrantData = null;
            MintingRequestData removedMintingRequestData = null;

            synchronized (addLockToLockMap(lockMintingRecordHashMap, currencyHash)) {
                MintingRecordData mintingRecordData = mintingRecords.getByHash(currencyHash);
                if (currencyHash == null || mintingRecordData == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_DELETE_REQUEST_INVALID_TOKEN, STATUS_ERROR));
                }
                HashMap<Hash, MintingFeeWarrantData> mintingFeeWarrants = mintingRecordData.getMintingFeeWarrants();
                if (mintingFeeWarrants == null || mintingFeeWarrants.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_DELETE_REQUEST_INVALID_FEE_WARRANT, STATUS_ERROR));
                }
                MintingFeeWarrantData mintingFeeWarrantData = mintingFeeWarrants.get(warrantFeeHash);
                if (mintingFeeWarrantData == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_DELETE_REQUEST_INVALID_FEE_WARRANT, STATUS_ERROR));
                }
                CurrencyData currencyData = currencies.getByHash(currencyHash);
                if (currencyData == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_DELETE_REQUEST_INVALID_CURRENCY, STATUS_ERROR));
                }
                if (!currencyData.getOriginatorHash().equals(userHash)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_DELETE_REQUEST_INVALID_ORIGINATOR, STATUS_ERROR));
                }
                MintingRequestData mintingRequestData = getMatchingMintingRequest(currencyHash, warrantFeeHash);
                if (mintingRequestData != null && mintingFeeWarrantData.isRunningAlready()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_DELETE_REQUEST_INVALID_FOR_EXISTING_TRANSACTION, STATUS_ERROR));
                }
                removedMintingFeeWarrantData = mintingFeeWarrants.remove(warrantFeeHash);
                if (removedMintingFeeWarrantData == null) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(TOKEN_MINTING_DELETE_REQUEST_FAILED_FEE_WARRANT_DELETION, STATUS_ERROR));
                }
                if (mintingRequestData != null) {
                    removedMintingRequestData =
                            mintingRecordData.getMintingRequests().remove(mintingRequestData.getTokenServiceFeeData().getHash());
                    if (removedMintingRequestData == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(TOKEN_MINTING_DELETE_REQUEST_FAILED_MINTED_REQUEST_DELETION, STATUS_ERROR));
                    }
                }
                mintingRecords.put(mintingRecordData);
                removeLockFromLocksMap(lockMintingRecordHashMap, currencyHash);
            }

            return ResponseEntity.ok(new DeleteTokenMintingQuoteResponse(removedMintingFeeWarrantData.getHash()));

        } catch (Exception e) {
            log.error("Error at deleting user minting tokens quote: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private MintingRequestData getMatchingMintingRequest(Hash currencyHash, Hash warrantFeeHash) {
        MintingRecordData mintingRecordData = mintingRecords.getByHash(currencyHash);
        if (mintingRecordData != null) {
            HashMap<Hash, MintingRequestData> mintingRequests = mintingRecordData.getMintingRequests();
            if (mintingRequests != null && !mintingRequests.isEmpty()) {
                return mintingRequests.values().stream().filter(mintingRequestData ->
                        warrantFeeHash.equals(mintingRequestData.getMintingFeeWarrantHash())
                ).findFirst().orElse(null);
            }
        }
        return null;
    }

    public ResponseEntity<IResponse> getTokenMintingFeeQuote(GetTokenMintingFeeQuoteRequest
                                                                     getTokenMintingFeeQuoteRequest) {
        try {
            if (!getTokenMintingFeeQuoteRequestCrypto.verifySignature(getTokenMintingFeeQuoteRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            Hash currencyHash = getTokenMintingFeeQuoteRequest.getCurrencyHash();

            CurrencyData currencyData = currencies.getByHash(currencyHash);
            if (currencyData == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_FEE_QUOTE_REQUEST_INVALID_CURRENCY, STATUS_ERROR));
            }
            if (!currencyData.getOriginatorHash().equals(getTokenMintingFeeQuoteRequest.getUserHash())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_FEE_QUOTE_REQUEST_INVALID_ORIGINATOR, STATUS_ERROR));
            }
            Instant requestTime = Instant.now();
            BigDecimal amountToMint = getTokenMintingFeeQuoteRequest.getAmount();
            BigDecimal feeQuoteAmount = feeService.calculateTokenMintingFee(amountToMint, requestTime, currencyData);
            MintingFeeWarrantData mintingFeeWarrantData = new MintingFeeWarrantData(currencyHash, requestTime, amountToMint, feeQuoteAmount);
            mintingFeeWarrantCrypto.signMessage(mintingFeeWarrantData);

            synchronized (addLockToLockMap(lockMintingRecordHashMap, currencyHash)) {
                MintingRecordData mintingRecordData = mintingRecords.getByHash(currencyHash);
                if (mintingRecordData == null) {
                    mintingRecordData = new MintingRecordData(currencyHash);
                }
                mintingRecords.put(mintingRecordData);
                removeLockFromLocksMap(lockMintingRecordHashMap, currencyHash);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(new GetTokenMintingFeeQuoteResponse(new MintingFeeQuoteResponseData(mintingFeeWarrantData)));

        } catch (Exception e) {
            log.error("Error at user minting tokens fee quote request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }
}
