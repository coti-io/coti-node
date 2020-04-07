package io.coti.financialserver.services;

import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.exceptions.CurrencyValidationException;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeCurrencyService;
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

import java.math.BigDecimal;
import java.util.*;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.financialserver.http.HttpStringConstants.*;

@Slf4j
@Service
public class CurrencyService extends BaseNodeCurrencyService {

    private static final String EXCEPTION_MESSAGE = "%s. Exception: %s";
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
    private FeeService feeService;
    @Autowired
    private IMintingService mintingService;

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
            originatorCurrencyData.validateName();
            originatorCurrencyData.validateSymbol();
            CurrencyTypeData currencyTypeData = generateTokenRequest.getCurrencyTypeData();
            if (currencyTypeData.getCurrencyType() != CurrencyType.REGULAR_CMD_TOKEN) {
                throw new CurrencyValidationException(TOKEN_GENERATION_REQUEST_NOT_REGULAR_CMD_TOKEN);
            }
            String currencyName = originatorCurrencyData.getName();
            currencyHash = originatorCurrencyData.calculateHash();
            validateCurrencyUniqueness(currencyHash, currencyName);
            CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(originatorCurrencyData.getSymbol(), currencyTypeData);
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

    private void validateTransactionAmount(OriginatorCurrencyData requestCurrencyData, Hash requestTransactionHash) {
        TransactionData tokenGenerationTransactionData = transactions.getByHash(requestTransactionHash);
        BaseTransactionData tokenServiceFeeData = tokenGenerationTransactionData.getBaseTransactions().stream()
                .filter(baseTransactionData -> baseTransactionData instanceof TokenFeeBaseTransactionData).findFirst().get();

        if (!tokenServiceFeeData.getAmount().equals(feeService.calculateTokenGenerationFee(requestCurrencyData.getTotalSupply()))) {
            throw new CurrencyException(String.format("The token generation fees in the transaction %s is not correct", requestTransactionHash));
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
