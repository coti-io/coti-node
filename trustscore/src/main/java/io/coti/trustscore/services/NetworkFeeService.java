package io.coti.trustscore.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.TransactionHelper;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.trustscore.http.HttpStringConstants.FULL_NODE_FEE_VALIDATION_ERROR;
import static io.coti.trustscore.http.HttpStringConstants.TRUST_SCORE_NOT_EXIST;

@Slf4j
@Service
public class NetworkFeeService {

    @Value("${network.fee.address}")
    private Hash networkFeeAddress;
    @Value("${network.fee.difference.validation}")
    private BigDecimal networkFeeDifferenceValidation;
    @Autowired
    private TransactionHelper transactionHelper;

    @Autowired
    private TrustScores trustScores;


    @Autowired
    private TrustScoreService trustScoreService;


    public ResponseEntity<IResponse> createNetworkFee(NetworkFeeRequest networkFeeRequest) {
        try {
            FullNodeFeeData fullNodeFeeData = networkFeeRequest.getFullNodeFeeData();
            if (!validateFullNodeFee(fullNodeFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(FULL_NODE_FEE_VALIDATION_ERROR,
                                STATUS_ERROR));
            }


            BigDecimal originalAmount = fullNodeFeeData.getOriginalAmount();
            BigDecimal reducedAmount = originalAmount.subtract(fullNodeFeeData.getAmount());

            TrustScoreData trustScoreData = trustScores.getByHash(networkFeeRequest.getUserHash());
            if (trustScoreData == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(String.format(TRUST_SCORE_NOT_EXIST, networkFeeRequest.getUserHash()), STATUS_ERROR));
            }
            double userTrustScore = trustScoreService.calculateUserTrustScore(trustScoreData);

            BigDecimal fee = calculateNetworkFeeAmount(getUserNetworkFeeByTrustScoreRange(userTrustScore), reducedAmount);
            NetworkFeeData networkFeeData = new NetworkFeeData(networkFeeAddress, fee, originalAmount, reducedAmount, new Date());
            setNetworkFeeHash(networkFeeData);
            signNetworkFee(networkFeeData, true);
            NetworkFeeResponseData networkFeeResponseData = new NetworkFeeResponseData(networkFeeData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new NetworkFeeResponse(networkFeeResponseData));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<IResponse> validateNetworkFee(NetworkFeeValidateRequest networkFeeValidateRequest) {
        try {
            FullNodeFeeData fullNodeFeeData = networkFeeValidateRequest.getFullNodeFeeData();
            if (!validateFullNodeFee(fullNodeFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new Response(FULL_NODE_FEE_VALIDATION_ERROR, STATUS_ERROR));
            }

            NetworkFeeData networkFeeData = networkFeeValidateRequest.getNetworkFeeData();
            boolean isValid = isNetworkFeeValid(networkFeeData, fullNodeFeeData.getAmount(), networkFeeValidateRequest.getUserHash());
            signNetworkFee(networkFeeData, isValid);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new NetworkFeeResponse(new NetworkFeeResponseData(networkFeeData)));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean validateFullNodeFee(FullNodeFeeData fullNodeFeeData) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(fullNodeFeeData);
        return BaseTransactionCrypto.FullNodeFeeData.isBaseTransactionValid(new TransactionData(baseTransactions), fullNodeFeeData);
    }

    private boolean isNetworkFeeValid(NetworkFeeData networkFeeData, BigDecimal fullNodeFeeAmount, Hash userHash) {

        return networkFeeData.getReducedAmount().equals(networkFeeData.getOriginalAmount().subtract(fullNodeFeeAmount))
                && isNetworkFeeValid(networkFeeData, userHash);
    }

    public boolean isNetworkFeeValid(NetworkFeeData networkFeeData, Hash userHash) {


        TrustScoreData trustScoreData = trustScores.getByHash(userHash);
        if (trustScoreData == null) {
            return false;
        }
        double userTrustScore = trustScoreService.calculateUserTrustScore(trustScoreData);

        BigDecimal calculatedNetworkFee = calculateNetworkFeeAmount(getUserNetworkFeeByTrustScoreRange(userTrustScore), networkFeeData.getReducedAmount());
        int compareResult = networkFeeDifferenceValidation.compareTo(calculatedNetworkFee.subtract(networkFeeData.getAmount()).abs());
        return compareResult >= 0 && validateNetworkFeeCrypto(networkFeeData);
    }

    private boolean validateNetworkFeeCrypto(NetworkFeeData networkFeeData) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(networkFeeData);
        return BaseTransactionCrypto.NetworkFeeData.isBaseTransactionValid(new TransactionData(baseTransactions), networkFeeData);
    }

    public boolean validateNetworkFee(NetworkFeeData networkFeeData) {
        return validateNetworkFeeCrypto(networkFeeData) && transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData);
    }

    public void setNetworkFeeHash(NetworkFeeData networkFeeData) throws ClassNotFoundException {
        BaseTransactionCrypto.NetworkFeeData.setBaseTransactionHash(networkFeeData);
    }


    public void signNetworkFee(NetworkFeeData networkFeeData, boolean isValid) throws ClassNotFoundException {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(networkFeeData);
        BaseTransactionCrypto.NetworkFeeData.signMessage(new TransactionData(baseTransactions), networkFeeData, new TrustScoreNodeResultData(NodeCryptoHelper.getNodeHash(), isValid));
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
        // formula calculation:
        return ((amount.multiply(userNetworkFeeByTrustScoreRange.getFeeRate())).divide(new BigDecimal(100)).max(userNetworkFeeByTrustScoreRange.getMinRate())).
                min(userNetworkFeeByTrustScoreRange.getMaxRate());
    }

}
