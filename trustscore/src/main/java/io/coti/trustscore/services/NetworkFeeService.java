package io.coti.trustscore.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.trustscore.config.rules.UserNetworkFeeByTrustScoreRange;
import io.coti.trustscore.data.Enums.TrustScoreRangeType;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.http.NetworkFeeRequest;
import io.coti.trustscore.http.NetworkFeeResponse;
import io.coti.trustscore.http.NetworkFeeValidateRequest;
import io.coti.trustscore.http.data.NetworkFeeResponseData;
import io.coti.trustscore.model.TrustScores;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.basenode.services.TransactionHelper.CURRENCY_SCALE;
import static io.coti.trustscore.http.HttpStringConstants.*;

@Slf4j
@Service
public class NetworkFeeService {

    @Value("${network.fee.address}")
    private Hash networkFeeAddress;
    @Value("${network.fee.difference.validation}")
    private BigDecimal networkFeeDifferenceValidation;
    @Value("${regular.token.network.fee}")
    private BigDecimal regularTokenNetworkFee;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private TrustScores trustScores;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private TrustScoreService trustScoreService;
    @Autowired
    private ICurrencyService currencyService;
    @Autowired
    protected Currencies currencies;

    public ResponseEntity<IResponse> createNetworkFee(NetworkFeeRequest networkFeeRequest) {
        try {
            Hash nativeCurrencyHash = currencyService.getNativeCurrencyHash();
            FullNodeFeeData fullNodeFeeData = networkFeeRequest.getFullNodeFeeData();
            boolean feeIncluded = networkFeeRequest.isFeeIncluded();
            if (!validateFullNodeFee(fullNodeFeeData, feeIncluded)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(FULL_NODE_FEE_VALIDATION_ERROR,
                                STATUS_ERROR));
            }

            BigDecimal originalAmount = fullNodeFeeData.getOriginalAmount();
            BigDecimal reducedAmount = null;

            if (feeIncluded) {
                reducedAmount = originalAmount.subtract(fullNodeFeeData.getAmount());

                if (reducedAmount.scale() > 0) {
                    reducedAmount = reducedAmount.stripTrailingZeros();
                }
            }

            BigDecimal fee;
            if (fullNodeFeeData.getOriginalCurrencyHash() == null || fullNodeFeeData.getOriginalCurrencyHash().equals(nativeCurrencyHash)) {
                TrustScoreData trustScoreData = trustScores.getByHash(networkFeeRequest.getUserHash());
                if (trustScoreData == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(String.format(TRUST_SCORE_NOT_EXIST, networkFeeRequest.getUserHash()), STATUS_ERROR));
                }
                double userTrustScore = trustScoreService.calculateUserTrustScore(trustScoreData);

                fee = calculateNetworkFeeAmount(getUserNetworkFeeByTrustScoreRange(userTrustScore), originalAmount);
            } else {
                CurrencyData currencyData = currencies.getByHash(fullNodeFeeData.getOriginalCurrencyHash());
                if (currencyData.getCurrencyTypeData().getCurrencyType() == CurrencyType.REGULAR_CMD_TOKEN) {
                    fee = regularTokenNetworkFee;
                } else {
                    // change it together with validateNetworkFee
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(String.format(UNDEFINED_TOKEN_TYPE_FEE, fullNodeFeeData.getCurrencyHash()), STATUS_ERROR));
                }
            }

            if (reducedAmount != null && reducedAmount.compareTo(fee) <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(String.format(INVALID_REDUCED_AMOUNT_VS_NETWORK_FEE,
                        fee.add(fullNodeFeeData.getAmount()).toPlainString()), STATUS_ERROR));
            }

            NetworkFeeData networkFeeData = new NetworkFeeData(networkFeeAddress, nativeCurrencyHash, fee, fullNodeFeeData.getOriginalCurrencyHash(), originalAmount, reducedAmount, Instant.now());
            setNetworkFeeHash(networkFeeData);
            signNetworkFee(networkFeeData, true);
            NetworkFeeResponseData networkFeeResponseData = new NetworkFeeResponseData(networkFeeData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new NetworkFeeResponse(networkFeeResponseData));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    public ResponseEntity<IResponse> validateNetworkFee(NetworkFeeValidateRequest networkFeeValidateRequest) {
        try {
            FullNodeFeeData fullNodeFeeData = networkFeeValidateRequest.getFullNodeFeeData();
            boolean feeIncluded = networkFeeValidateRequest.isFeeIncluded();

            if (!validateFullNodeFee(fullNodeFeeData, feeIncluded)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(FULL_NODE_FEE_VALIDATION_ERROR, STATUS_ERROR));
            }

            NetworkFeeData networkFeeData = networkFeeValidateRequest.getNetworkFeeData();
            boolean isValid = isNetworkFeeValid(networkFeeData, fullNodeFeeData, networkFeeValidateRequest.getUserHash(), feeIncluded);
            signNetworkFee(networkFeeData, isValid);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new NetworkFeeResponse(new NetworkFeeResponseData(networkFeeData)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private boolean validateFullNodeFee(FullNodeFeeData fullNodeFeeData, boolean feeIncluded) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(fullNodeFeeData);
        return validationService.validateAmountField(fullNodeFeeData.getAmount()) && validationService.validateAmountField(fullNodeFeeData.getOriginalAmount())
                && (!feeIncluded || fullNodeFeeData.getOriginalAmount().compareTo(fullNodeFeeData.getAmount()) > 0)
                && BaseTransactionCrypto.FULL_NODE_FEE_DATA.isBaseTransactionValid(new TransactionData(baseTransactions), fullNodeFeeData);
    }

    private boolean isNetworkFeeValid(NetworkFeeData networkFeeData, FullNodeFeeData fullNodeFeeData, Hash userHash, boolean feeIncluded) {
        BigDecimal reducedAmount = networkFeeData.getOriginalAmount().subtract(fullNodeFeeData.getAmount());
        return validationService.validateAmountField(networkFeeData.getAmount())
                && networkFeeData.getOriginalAmount().equals(fullNodeFeeData.getOriginalAmount())
                && (!feeIncluded || (validationService.validateAmountField(networkFeeData.getReducedAmount())
                && networkFeeData.getReducedAmount().equals(reducedAmount.scale() > 0 ? reducedAmount.stripTrailingZeros() : reducedAmount)
                && networkFeeData.getReducedAmount().compareTo(networkFeeData.getAmount()) > 0))
                && isNetworkFeeValid(networkFeeData, userHash);
    }

    private boolean isNetworkFeeValid(NetworkFeeData networkFeeData, Hash userHash) {
        Hash nativeCurrencyHash = currencyService.getNativeCurrencyHash();
        BigDecimal calculatedNetworkFee;
        if (networkFeeData.getOriginalCurrencyHash() == null || networkFeeData.getOriginalCurrencyHash().equals(nativeCurrencyHash)) {
            TrustScoreData trustScoreData = trustScores.getByHash(userHash);
            if (trustScoreData == null) {
                return false;
            }
            double userTrustScore = trustScoreService.calculateUserTrustScore(trustScoreData);
            calculatedNetworkFee = calculateNetworkFeeAmount(getUserNetworkFeeByTrustScoreRange(userTrustScore), networkFeeData.getOriginalAmount());
            CurrencyData currencyData = currencies.getByHash(networkFeeData.getOriginalCurrencyHash());
        } else {
            if (currencyData.getCurrencyTypeData().getCurrencyType() == CurrencyType.REGULAR_CMD_TOKEN){
                calculatedNetworkFee = regularTokenNetworkFee;
            } else {
                return false;   // change it together with createNetworkFee
            }
        }

        int compareResult = networkFeeDifferenceValidation.compareTo(calculatedNetworkFee.subtract(networkFeeData.getAmount()).abs());
        return compareResult >= 0 && validateNetworkFeeCrypto(networkFeeData);
    }

    private boolean validateNetworkFeeCrypto(NetworkFeeData networkFeeData) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(networkFeeData);
        return BaseTransactionCrypto.NETWORK_FEE_DATA.isBaseTransactionValid(new TransactionData(baseTransactions), networkFeeData);
    }

    public boolean validateNetworkFee(NetworkFeeData networkFeeData) {
        return validationService.validateAmountField(networkFeeData.getOriginalAmount())
                && validationService.validateAmountField(networkFeeData.getAmount())
                && validationService.validateAmountField(networkFeeData.getReducedAmount())
                && validateNetworkFeeCrypto(networkFeeData) && transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData);
    }

    private void setNetworkFeeHash(NetworkFeeData networkFeeData) {
        BaseTransactionCrypto.NETWORK_FEE_DATA.setBaseTransactionHash(networkFeeData);
    }


    private void signNetworkFee(NetworkFeeData networkFeeData, boolean isValid) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(networkFeeData);
        BaseTransactionCrypto.NETWORK_FEE_DATA.signMessage(new TransactionData(baseTransactions), networkFeeData, new TrustScoreNodeResultData(NodeCryptoHelper.getNodeHash(), isValid));
    }


    private UserNetworkFeeByTrustScoreRange getUserNetworkFeeByTrustScoreRange(double trustScore) {
        Map<TrustScoreRangeType, UserNetworkFeeByTrustScoreRange> trustScoreRangeTypeToUserScoreMap
                = trustScoreService.getRulesData().getTrustScoreRangeTypeToUserScoreMap();
        if (trustScore < trustScoreRangeTypeToUserScoreMap.get(TrustScoreRangeType.LOW).getLimit()) {
            return trustScoreRangeTypeToUserScoreMap.get(TrustScoreRangeType.LOW);
        } else if (trustScore > trustScoreRangeTypeToUserScoreMap.get(TrustScoreRangeType.HIGH).getLimit()) {
            return trustScoreRangeTypeToUserScoreMap.get(TrustScoreRangeType.HIGH);
        }
        return trustScoreRangeTypeToUserScoreMap.get(TrustScoreRangeType.STANDARD);
    }

    private BigDecimal calculateNetworkFeeAmount(UserNetworkFeeByTrustScoreRange userNetworkFeeByTrustScoreRange, BigDecimal amount) {
        BigDecimal fee = ((amount.multiply(userNetworkFeeByTrustScoreRange.getFeeRate())).divide(new BigDecimal(100)).max(userNetworkFeeByTrustScoreRange.getMinRate())).
                min(userNetworkFeeByTrustScoreRange.getMaxRate());
        if (fee.scale() > CURRENCY_SCALE) {
            fee = fee.setScale(CURRENCY_SCALE, RoundingMode.DOWN);
        }
        if (fee.scale() > 0) {
            fee = fee.stripTrailingZeros();
        }
        return fee;
    }

}
