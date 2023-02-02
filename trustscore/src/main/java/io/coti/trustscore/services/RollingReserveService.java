package io.coti.trustscore.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.RollingReserveException;
import io.coti.basenode.http.*;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.http.RollingReserveRequest;
import io.coti.trustscore.http.RollingReserveResponse;
import io.coti.trustscore.http.RollingReserveValidateRequest;
import io.coti.trustscore.http.data.RollingReserveResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.services.BaseNodeTransactionHelper.CURRENCY_SCALE;
import static io.coti.trustscore.http.HttpStringConstants.*;
import static io.coti.trustscore.services.NodeServiceManager.*;

@Slf4j
@Service
public class RollingReserveService {

    private static final double MAX_ROLLING_RESERVE_RATE = 100;
    private static final String MERCHANT_ADDRESS_END_POINT = "/rollingReserve/merchantReserveAddress";
    @Value("${rolling.reserve.difference.validation}")
    private BigDecimal rollingReserveDifferenceValidation;

    public ResponseEntity<IResponse> createRollingReserveFee(RollingReserveRequest rollingReserveRequest) {

        try {
            Hash nativeCurrencyHash = currencyService.getNativeCurrencyHash();
            NetworkFeeData networkFeeData = rollingReserveRequest.getNetworkFeeData();
            if (!feeService.validateNetworkFee(networkFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(NETWORK_FEE_VALIDATION_ERROR, STATUS_ERROR));
            }

            TrustScoreData trustScoreData = trustScores.getByHash(rollingReserveRequest.getMerchantHash());

            if (trustScoreData == null || !trustScoreData.getUserType().equals(UserType.MERCHANT)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(String.format(USER_NOT_MERCHANT, rollingReserveRequest.getMerchantHash()), STATUS_ERROR));
            }

            BigDecimal originalAmount = networkFeeData.getOriginalAmount();
            BigDecimal reducedAmount = networkFeeData.getReducedAmount().subtract(networkFeeData.getAmount());

            if (reducedAmount.scale() > 0) {
                reducedAmount = reducedAmount.stripTrailingZeros();
            }

            Hash rollingReserveAddress = getMerchantRollingReserveAddress(rollingReserveRequest.getMerchantHash());
            BigDecimal rollingReserveAmount = calculateRollingReserveAmount(reducedAmount, trustScoreService.calculateUserTrustScore(trustScoreData));

            RollingReserveData rollingReserveData = new RollingReserveData(rollingReserveAddress, nativeCurrencyHash, rollingReserveAmount, nativeCurrencyHash, originalAmount, reducedAmount, Instant.now());
            setRollingReserveNodeFeeHash(rollingReserveData);
            signRollingReserveFee(rollingReserveData, true);
            RollingReserveResponseData rollingReserveResponseData = new RollingReserveResponseData(rollingReserveData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RollingReserveResponse(rollingReserveResponseData));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private Hash getMerchantRollingReserveAddress(Hash merchantHash) {
        MerchantRollingReserveAddressData merchantRollingReserveAddressData = merchantRollingReserveAddresses.getByHash(merchantHash);
        if (merchantRollingReserveAddressData == null) {
            merchantRollingReserveAddressData = getMerchantAddressFromFinancialNode(merchantHash);
            merchantRollingReserveAddresses.put(merchantRollingReserveAddressData);
        }

        return merchantRollingReserveAddressData.getMerchantRollingReserveAddress();
    }

    private MerchantRollingReserveAddressData getMerchantAddressFromFinancialNode(Hash merchantHash) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new CustomHttpComponentsClientHttpRequestFactory());
        GetMerchantRollingReserveAddressRequest getMerchantRollingReserveAddressRequest = new GetMerchantRollingReserveAddressRequest();
        getMerchantRollingReserveAddressRequest.setMerchantHash(merchantHash);

        getMerchantRollingReserveAddressCrypto.signMessage(getMerchantRollingReserveAddressRequest);

        try {
            NetworkNodeData financialServer = networkService.getSingleNodeData(NodeType.FinancialServer);
            String financialServerHttpAddress = financialServer.getHttpFullAddress();
            GetMerchantRollingReserveAddressResponse result = restTemplate.postForObject(financialServerHttpAddress + MERCHANT_ADDRESS_END_POINT, getMerchantRollingReserveAddressRequest, GetMerchantRollingReserveAddressResponse.class);
            if (result == null) {
                throw new RollingReserveException(String.format(MERCHANT_ADDRESS_GET_ERROR, "Null result from financial server"));
            }
            return result.getMerchantRollingReserveAddressData();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RollingReserveException(String.format(MERCHANT_ADDRESS_GET_ERROR, ((SerializableResponse) jacksonSerializer.deserialize(e.getResponseBodyAsByteArray())).getMessage()));
        }


    }

    public ResponseEntity<IResponse> validateRollingReserve(RollingReserveValidateRequest rollingReserveValidateRequest) {
        try {

            NetworkFeeData networkFeeData = rollingReserveValidateRequest.getNetworkFeeData();
            if (!feeService.validateNetworkFee(networkFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(NETWORK_FEE_VALIDATION_ERROR, STATUS_ERROR));
            }
            TrustScoreData trustScoreData = trustScores.getByHash(rollingReserveValidateRequest.getMerchantHash());

            RollingReserveData rollingReserveData = rollingReserveValidateRequest.getRollingReserveData();
            boolean isValid = isRollingReserveValid(rollingReserveData, networkFeeData, trustScoreService.calculateUserTrustScore(trustScoreData), trustScoreData.getUserType());
            signRollingReserveFee(rollingReserveData, isValid);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RollingReserveResponse(new RollingReserveResponseData(rollingReserveData)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }

    }

    private void setRollingReserveNodeFeeHash(RollingReserveData rollingReserveData) {
        BaseTransactionCrypto.ROLLING_RESERVE_DATA.createAndSetBaseTransactionHash(rollingReserveData);
    }

    private void signRollingReserveFee(RollingReserveData rollingReserveData, boolean isValid) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(rollingReserveData);
        BaseTransactionCrypto.ROLLING_RESERVE_DATA.signMessage(new TransactionData(baseTransactions), rollingReserveData, new TrustScoreNodeResultData(nodeIdentityService.getNodeHash(), isValid));
    }

    private boolean isRollingReserveValid(RollingReserveData rollingReserveData, NetworkFeeData networkFeeData, double userTrustScore, UserType userType) {
        return userType.equals(UserType.MERCHANT)
                && validationService.validateAmountField(rollingReserveData.getReducedAmount())
                && validationService.validateAmountField(rollingReserveData.getAmount())
                && rollingReserveData.getOriginalAmount().equals(networkFeeData.getOriginalAmount())
                && rollingReserveData.getReducedAmount().equals(networkFeeData.getReducedAmount().subtract(networkFeeData.getAmount()))
                && isRollingReserveValid(rollingReserveData, userTrustScore);
    }

    private boolean isRollingReserveValid(RollingReserveData rollingReserveData, double userTrustScore) {
        BigDecimal calculatedReserve = calculateRollingReserveAmount(rollingReserveData.getReducedAmount(), userTrustScore);
        int compareResult = rollingReserveDifferenceValidation.compareTo(calculatedReserve.subtract(rollingReserveData.getAmount()).abs());
        return compareResult >= 0 && validateRollingReserveCrypto(rollingReserveData);
    }

    private boolean validateRollingReserveCrypto(RollingReserveData rollingReserveData) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(rollingReserveData);
        return BaseTransactionCrypto.ROLLING_RESERVE_DATA.isBaseTransactionValid(new TransactionData(baseTransactions), rollingReserveData);
    }

    private BigDecimal calculateRollingReserveAmount(BigDecimal reducedAmount, double trustScore) {
        double reserveRate = (BigDecimal.valueOf(trustScore).compareTo(BigDecimal.ZERO) == 0) ? MAX_ROLLING_RESERVE_RATE : Math.min(MAX_ROLLING_RESERVE_RATE / trustScore, MAX_ROLLING_RESERVE_RATE);
        BigDecimal rollingReserveAmount = reducedAmount.multiply(BigDecimal.valueOf(reserveRate / 100));
        if (rollingReserveAmount.scale() > CURRENCY_SCALE) {
            rollingReserveAmount = rollingReserveAmount.setScale(CURRENCY_SCALE, RoundingMode.DOWN);
        }
        if (rollingReserveAmount.scale() > 0) {
            rollingReserveAmount = rollingReserveAmount.stripTrailingZeros();
        }
        return rollingReserveAmount;
    }
}
