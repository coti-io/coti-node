package io.coti.financialserver.services;

import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.CurrencyValidationException;
import io.coti.basenode.http.GenerateTokenFeeRequest;
import io.coti.basenode.http.GetCurrenciesRequest;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeCurrencyService;
import io.coti.financialserver.http.GetCurrenciesResponse;
import io.coti.financialserver.http.data.GetCurrencyResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.financialserver.http.HttpStringConstants.*;
import static io.coti.financialserver.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class CurrencyService extends BaseNodeCurrencyService {

    private static final String EXCEPTION_MESSAGE = "%s. Exception: %s";

    @Override
    public ResponseEntity<IResponse> getTokenGenerationFee(GenerateTokenFeeRequest generateTokenRequest) {
        Hash currencyHash;
        try {
            if (!nodeEventService.eventHappened(Event.MULTI_DAG)) {
                throw new CurrencyValidationException(MULTI_DAG_IS_NOT_SUPPORTED);
            }
            OriginatorCurrencyData originatorCurrencyData = generateTokenRequest.getOriginatorCurrencyData();
            CurrencyTypeData currencyTypeData = generateTokenRequest.getCurrencyTypeData();
            if (!currencyTypeData.getCurrencyType().equals(CurrencyType.REGULAR_CMD_TOKEN)) {
                throw new CurrencyValidationException(TOKEN_GENERATION_REQUEST_NOT_REGULAR_CMD_TOKEN);
            }
            String currencyName = originatorCurrencyData.getName();
            currencyHash = OriginatorCurrencyCrypto.calculateHash(originatorCurrencyData.getSymbol());
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

    @Override
    public ResponseEntity<IResponse> getCurrenciesForWallet(GetCurrenciesRequest getCurrenciesRequest) {
        if (!nodeEventService.eventHappened(Event.MULTI_DAG)) {
            return ResponseEntity.badRequest().body(new Response(MULTI_DAG_IS_NOT_SUPPORTED, STATUS_ERROR));
        }
        List<GetCurrencyResponseData> tokenDetails = new ArrayList<>();
        getCurrenciesRequest.getTokenHashes().forEach(tokenHash -> {
            if (!isNativeCurrency(tokenHash)) {
                CurrencyData tokenData = currencies.getByHash(tokenHash);
                if (tokenData != null) {
                    tokenDetails.add(new GetCurrencyResponseData(tokenData));
                }
            }
        });
        tokenDetails.sort(Comparator.comparing(GetCurrencyResponseData::getName));
        return ResponseEntity.status(HttpStatus.OK).body(new GetCurrenciesResponse(new GetCurrencyResponseData(nativeCurrencyName, nativeCurrencySymbol), tokenDetails));
    }

}
