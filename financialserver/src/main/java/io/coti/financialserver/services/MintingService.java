package io.coti.financialserver.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TokenMintingCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.CurrencyValidationException;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeMintingService;
import io.coti.financialserver.crypto.GetMintingHistoryRequestCrypto;
import io.coti.financialserver.crypto.GetTokenMintingFeeQuoteRequestCrypto;
import io.coti.financialserver.crypto.MintingFeeQuoteCrypto;
import io.coti.financialserver.data.MintingFeeQuoteData;
import io.coti.financialserver.data.MintingHistoryData;
import io.coti.financialserver.data.MintingRecordData;
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
import java.time.temporal.ChronoUnit;
import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class MintingService extends BaseNodeMintingService {

    private static final int MINTING_FEE_QUOTE_EXPIRATION_MINUTES = 60;
    @Value("${financialserver.seed}")
    private String seed;
    @Autowired
    private TokenMintingCrypto tokenMintingCrypto;
    @Autowired
    private UserTokenGenerations userTokenGenerations;
    @Autowired
    private FeeService feeService;
    @Autowired
    private MintingRecords mintingRecords;
    @Autowired
    private GetMintingHistoryRequestCrypto getMintingHistoryRequestCrypto;
    @Autowired
    private GetTokenMintingFeeQuoteRequestCrypto getTokenMintingFeeQuoteRequestCrypto;
    @Autowired
    private MintingFeeQuoteCrypto mintingFeeQuoteCrypto;

    public ResponseEntity<IResponse> getTokenMintingFee(TokenMintingFeeRequest tokenMintingFeeRequest) {
        try {
            TokenMintingData tokenMintingData = tokenMintingFeeRequest.getTokenMintingData();
            CurrencyData currencyData = currencies.getByHash(tokenMintingData.getMintingCurrencyHash());

            ResponseEntity<IResponse> badRequestResponse = validateTokenMintingFeeRequest(tokenMintingFeeRequest, currencyData);
            if (badRequestResponse != null) return badRequestResponse;

            return createTokenMintingFee(tokenMintingData, currencyData, tokenMintingFeeRequest.getMintingFeeQuoteData());
        } catch (CurrencyValidationException e) {
            String error = String.format("%s. Exception: %s", TOKEN_MINTING_FAILURE, e.getMessageAndCause());
            return ResponseEntity.badRequest().body(new Response(error, STATUS_ERROR));
        } catch (CotiRunTimeException e) {
            String error = String.format("%s. Exception: %s", TOKEN_MINTING_FAILURE, e.getMessageAndCause());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        } catch (Exception e) {
            String error = String.format("%s. Exception: %s", TOKEN_MINTING_FAILURE, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(error, STATUS_ERROR));
        }
    }

    private ResponseEntity<IResponse> validateTokenMintingFeeRequest(TokenMintingFeeRequest tokenMintingFeeRequest, CurrencyData currencyData) {
        TokenMintingData tokenMintingData = tokenMintingFeeRequest.getTokenMintingData();
        MintingFeeQuoteData mintingFeeQuoteData = tokenMintingFeeRequest.getMintingFeeQuoteData();
        if (!tokenMintingCrypto.verifySignature(tokenMintingData)) {
            throw new CurrencyValidationException(TOKEN_MINTING_REQUEST_INVALID_SIGNATURE);
        }
        if (mintingFeeQuoteData != null && !mintingFeeQuoteCrypto.verifySignature(mintingFeeQuoteData)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
        }
        if (currencyData == null) {
            throw new CurrencyValidationException(TOKEN_MINTING_REQUEST_INVALID_CURRENCY);
        }
        if (!currencyData.getOriginatorHash().equals(tokenMintingData.getSignerHash())) {
            throw new CurrencyValidationException(TOKEN_MINTING_REQUEST_INVALID_ORIGINATOR);
        }
        if (!isAddressValid(tokenMintingFeeRequest.getTokenMintingData().getReceiverAddress())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_ADDRESS, STATUS_ERROR));
        }

        if (mintingFeeQuoteData != null && (!isStillValid(mintingFeeQuoteData) ||
                !mintingFeeQuoteData.getMintingAmount().equals(tokenMintingData.getMintingAmount())
                || !mintingFeeQuoteData.getCurrencyHash().equals(tokenMintingData.getMintingCurrencyHash())
                || !mintingFeeQuoteData.getMintingFee().equals(tokenMintingData.getFeeAmount()))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_FOR_THE_QUOTE, STATUS_ERROR));
        }
        BigDecimal mintableAmount = Optional.ofNullable(currencyService.getTokenMintableAmount(tokenMintingData.getMintingCurrencyHash())).orElse(BigDecimal.ZERO);
        if (mintableAmount.subtract(tokenMintingData.getMintingAmount()).signum() < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_AMOUNT, STATUS_ERROR));
        }
        return null;
    }

    private ResponseEntity<IResponse> createTokenMintingFee(TokenMintingData tokenMintingData, CurrencyData currencyData, MintingFeeQuoteData mintingFeeQuoteData) {
        try {
            BigDecimal mintingFee = null;
            if (mintingFeeQuoteData != null) {
                mintingFee = mintingFeeQuoteData.getMintingFee();
            }
            if (mintingFee == null) {
                mintingFee = feeService.calculateTokenMintingFee(tokenMintingData.getMintingAmount(), Instant.now(), currencyData);
            }
            TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = new TokenMintingFeeBaseTransactionData(feeService.networkFeeAddress(),
                    currencyService.getNativeCurrencyHash(), NodeCryptoHelper.getNodeHash(), mintingFee, Instant.now(), tokenMintingData);
            setTokenMintingFeeHash(tokenMintingFeeBaseTransactionData);
            signTokenMintingFee(tokenMintingFeeBaseTransactionData);
            TokenMintingFeeResponseData tokenMintingFeeResponseData = new TokenMintingFeeResponseData(tokenMintingFeeBaseTransactionData);
            return ResponseEntity.status(HttpStatus.CREATED).body(new TokenMintingFeeResponse(tokenMintingFeeResponseData));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private boolean isAddressValid(Hash address) {
        return CryptoHelper.isAddressValid(address);
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

            Map<Hash, Map<Instant, MintingHistoryData>> mintingHistory = new HashMap<>();
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

    private void fillMintingHistory(Hash currencyHash, Map<Hash, Map<Instant, MintingHistoryData>> mintingHistory) {
        MintingRecordData mintingRecordData = mintingRecords.getByHash(currencyHash);
        if (mintingRecordData != null) {
            Map<Instant, MintingHistoryData> mintingTokenHistory = mintingRecordData.getMintingHistory();
            mintingHistory.put(currencyHash, mintingTokenHistory);
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
            BigDecimal mintableAmount = Optional.ofNullable(currencyService.getTokenMintableAmount(currencyHash)).orElse(BigDecimal.ZERO);
            BigDecimal mintingAmount = getTokenMintingFeeQuoteRequest.getMintingAmount();
            if (mintableAmount.subtract(mintingAmount).signum() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TOKEN_MINTING_REQUEST_INVALID_AMOUNT, STATUS_ERROR));
            }
            Instant createTime = Instant.now();
            BigDecimal feeQuoteAmount = feeService.calculateTokenMintingFee(mintingAmount, createTime, currencyData);
            MintingFeeQuoteData mintingFeeQuoteData = new MintingFeeQuoteData(currencyHash, createTime, mintingAmount, feeQuoteAmount);
            mintingFeeQuoteCrypto.signMessage(mintingFeeQuoteData);

            return ResponseEntity.status(HttpStatus.CREATED).body(new GetTokenMintingFeeQuoteResponse(new MintingFeeQuoteResponseData(mintingFeeQuoteData)));
        } catch (Exception e) {
            log.error("Error at user minting tokens fee quote request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private void setTokenMintingFeeHash(TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData) {
        BaseTransactionCrypto.TOKEN_MINTING_BASE_TRANSACTION_DATA.createAndSetBaseTransactionHash(tokenMintingFeeBaseTransactionData);
    }

    private void signTokenMintingFee(TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData) {
        tokenMintingFeeBaseTransactionData.setSignature(NodeCryptoHelper.signMessage(tokenMintingFeeBaseTransactionData.getHash().getBytes()));
    }

    private boolean isStillValid(MintingFeeQuoteData mintingFeeQuoteData) {
        Instant createTime = mintingFeeQuoteData.getCreateTime();
        return createTime.isAfter(Instant.now().minus(MINTING_FEE_QUOTE_EXPIRATION_MINUTES, ChronoUnit.MINUTES)) && createTime.isBefore(Instant.now());
    }

}
