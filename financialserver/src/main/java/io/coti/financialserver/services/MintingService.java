package io.coti.financialserver.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.CurrencyValidationException;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeMintingService;
import io.coti.financialserver.crypto.GetMintingHistoryRequestCrypto;
import io.coti.financialserver.crypto.GetTokenMintingFeeQuoteRequestCrypto;
import io.coti.financialserver.crypto.MintingFeeWarrantCrypto;
import io.coti.financialserver.crypto.MintingTokenFeeRequestCrypto;
import io.coti.financialserver.data.MintedTokenData;
import io.coti.financialserver.data.MintingFeeWarrantData;
import io.coti.financialserver.data.MintingRecordData;
import io.coti.financialserver.data.ReservedAddress;
import io.coti.financialserver.http.*;
import io.coti.financialserver.http.data.MintingFeeQuoteResponseData;
import io.coti.financialserver.http.data.TokenMintingFeeResponseData;
import io.coti.financialserver.model.MintingRecords;
import io.coti.financialserver.model.UserTokenGenerations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class MintingService extends BaseNodeMintingService {

    @Value("${financialserver.seed}")
    private String seed;

    @Autowired
    private MintingTokenFeeRequestCrypto mintingTokenFeeRequestCrypto;
    @Autowired
    private UserTokenGenerations userTokenGenerations;
    @Autowired
    private FeeService feeService;
    @Autowired
    private MintingRecords mintingRecords;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private GetMintingHistoryRequestCrypto getMintingHistoryRequestCrypto;
    @Autowired
    private GetTokenMintingFeeQuoteRequestCrypto getTokenMintingFeeQuoteRequestCrypto;
    @Autowired
    private MintingFeeWarrantCrypto mintingFeeWarrantCrypto;

    private BlockingQueue<TransactionData> confirmedTokenMintingFeeTransactionQueue;
    private Thread confirmedTokenMintingFeeTransactionThread;

    @Override
    public void init() {
        super.init();
        initQueuesAndThreads();
    }

    private void initQueuesAndThreads() {
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

    public void addToConfirmedTokenMintingFeeTransactionQueue(TransactionData transactionData) {
        addToTransactionQueue(confirmedTokenMintingFeeTransactionQueue, transactionData);
    }

    public ResponseEntity<IResponse> getTokenMintingFee(MintingTokenFeeRequest mintingTokenFeeRequest) {
        CurrencyData currencyData;
        try {
            currencyData = validateMintingTokenFeeRequestAndGetCurrencyData(mintingTokenFeeRequest);
            if (!validateTokenSupplyAvailableAndWarrantAmount(mintingTokenFeeRequest, currencyData)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_FOR_WARRANT, STATUS_ERROR));
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
                mintingFee = mintingFeeWarrantData.getFeeForMinting();
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

    public ResponseEntity<IResponse> createTokenMintingFeeAndMintingRequest(MintingTokenFeeRequest mintingTokenFeeRequest, CurrencyData currencyData, BigDecimal mintingFee)
            throws ClassNotFoundException {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = new TokenMintingFeeBaseTransactionData(feeService.networkFeeAddress(), currencyService.getNativeCurrencyHash(),
                NodeCryptoHelper.getNodeHash(), mintingFee, Instant.now(), new TokenMintingFeeDataInBaseTransaction(currencyData.getHash(), mintingTokenFeeRequest.getMintingFeeData().getAmount(), mintingTokenFeeRequest.getReceiverAddress(),
                mintingTokenFeeRequest.getMintingFeeData().getCreationTime(), mintingFee));
        setFeeHash(tokenMintingFeeBaseTransactionData);
        signTokenGenerationFee(tokenMintingFeeBaseTransactionData);
        TokenMintingFeeResponseData tokenMintingFeeResponseData = new TokenMintingFeeResponseData(tokenMintingFeeBaseTransactionData);
        return ResponseEntity.status(HttpStatus.CREATED).body(new TokenMintingFeeResponse(tokenMintingFeeResponseData));
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

        synchronized (addLockToLockMap(currencyData.getHash())) {
            mintingRecordData = mintingRecords.getByHash(currencyData.getHash());
            if (mintingRecordData == null) {
                mintingRecordData = new MintingRecordData(currencyData.getHash());
                mintingRecords.put(mintingRecordData);
            }
            removeLockFromLocksMap(currencyData.getHash());
        }

        if (mintingTokenFeeRequest.getMintingFeeWarrantData() != null) {
            MintingFeeWarrantData mintingFeeWarrantData = mintingTokenFeeRequest.getMintingFeeWarrantData();
            if (!mintingFeeWarrantData.getAmount().equals(mintingTokenFeeRequest.getMintingFeeData().getAmount())
                    || !mintingFeeWarrantData.getCurrencyHash().equals(mintingTokenFeeRequest.getMintingFeeData().getCurrencyHash())
                    || !currencyData.getOriginatorHash().equals(mintingTokenFeeRequest.getUserHash())) {
                return false;
            }
        }
        return true;
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
        synchronized (addLockToLockMap(currencyHash)) {
            MintingRecordData mintingRecordData = mintingRecords.getByHash(currencyHash);
            if (mintingRecordData != null) {
                Map<Instant, MintedTokenData> mintingTokenHistory = mintingRecordData.getMintingHistory();
                mintingHistory.put(currencyHash, mintingTokenHistory);
                mintingRecords.put(mintingRecordData);
            }
            removeLockFromLocksMap(currencyHash);
        }
    }

    private void handleConfirmedTokenMintingTransactions() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TransactionData tokenMintingTransaction = confirmedTokenMintingFeeTransactionQueue.take();
                TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = getTokenMintingFeeData(tokenMintingTransaction);
                if (tokenMintingFeeBaseTransactionData == null) {
                    log.error("TokenMinting transaction {} without TMBT", tokenMintingTransaction.getHash());
                    continue;
                }
                TokenMintingFeeDataInBaseTransaction tokenMintingFeeBaseTransactionServiceData = tokenMintingFeeBaseTransactionData.getServiceData();
                Hash mintingCurrencyHash = tokenMintingFeeBaseTransactionServiceData.getMintingCurrencyHash();
                synchronized (addLockToLockMap(mintingCurrencyHash)) {
                    MintingRecordData mintingRecordData = mintingRecords.getByHash(mintingCurrencyHash);
                    if (mintingRecordData == null) {
                        log.error("TokenMinting transaction {} without minting record", tokenMintingTransaction.getHash());
                        continue;
                    }

                    int genesisAddressIndex = Math.toIntExact(ReservedAddress.GENESIS_ONE.getIndex());
                    Hash cotiGenesisAddress = nodeCryptoHelper.generateAddress(seed, genesisAddressIndex);
                    BigDecimal newAmountToMint = tokenMintingFeeBaseTransactionServiceData.getMintingAmount();
                    Hash initialTransactionHash = transactionCreationService.createInitialTransaction(newAmountToMint, mintingCurrencyHash,
                            cotiGenesisAddress, tokenMintingFeeBaseTransactionServiceData.getReceiverAddress(), genesisAddressIndex);

                    if (initialTransactionHash != null) {
                        log.info("Minting transaction {} for token {} successfully created. The amount is {}", initialTransactionHash,
                                mintingCurrencyHash, newAmountToMint);
                        MintedTokenData mintedTokenData = new MintedTokenData(mintingCurrencyHash,
                                Instant.now(), newAmountToMint, initialTransactionHash, tokenMintingTransaction.getHash());
                        mintingRecordData.getMintingHistory().put(mintedTokenData.getMintingTime(), mintedTokenData);
                        mintingRecords.put(mintingRecordData);
                    }
                    removeLockFromLocksMap(mintingCurrencyHash);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public ResponseEntity<IResponse> getTokenMintingFeeQuote(GetTokenMintingFeeQuoteRequest getTokenMintingFeeQuoteRequest) {
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

            return ResponseEntity.status(HttpStatus.CREATED).body(new GetTokenMintingFeeQuoteResponse(new MintingFeeQuoteResponseData(mintingFeeWarrantData)));
        } catch (Exception e) {
            log.error("Error at user minting tokens fee quote request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    protected void setFeeHash(TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData) throws ClassNotFoundException {
        BaseTransactionCrypto.TokenMintingFeeBaseTransactionData.setBaseTransactionHash(tokenMintingFeeBaseTransactionData);
    }

    protected void signTokenGenerationFee(TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData) {
        tokenMintingFeeBaseTransactionData.setSignature(nodeCryptoHelper.signMessage(tokenMintingFeeBaseTransactionData.getHash().getBytes()));
    }
}
