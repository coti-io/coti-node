package io.coti.trustscore.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.services.TransactionHelper;
import io.coti.trustscore.http.NetworkFeeRequest;
import io.coti.trustscore.http.NetworkFeeResponse;
import io.coti.trustscore.http.NetworkFeeValidateRequest;
import io.coti.trustscore.http.data.NetworkFeeResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.trustscore.http.HttpStringConstants.FULL_NODE_FEE_VALIDATION_ERROR;

@Service
public class FeeService {

    @Value("${network.fee.address}")
    private Hash networkFeeAddress;
    @Value("${network.fee.difference.validation}")
    private BigDecimal networkFeeDifferenceValidation;
    @Autowired
    private TransactionHelper transactionHelper;

    public ResponseEntity<Response> createNetworkFee(NetworkFeeRequest networkFeeRequest) {
        try {
            FullNodeFeeData fullNodeFeeData = networkFeeRequest.getFullNodeFeeData();
            if (!validateFullNodeFee(fullNodeFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new NetworkFeeResponse(
                                STATUS_ERROR,
                                FULL_NODE_FEE_VALIDATION_ERROR));
            }
            BigDecimal originalAmount = fullNodeFeeData.getOriginalAmount();
            BigDecimal reducedAmount = originalAmount.subtract(fullNodeFeeData.getAmount());
            BigDecimal fee = calculateNetworkFee(reducedAmount);
            NetworkFeeData networkFeeData = new NetworkFeeData(networkFeeAddress, fee, originalAmount, reducedAmount, new Date());
            setNetworkFeeHash(networkFeeData);
            signNetworkFee(networkFeeData, true);
            NetworkFeeResponseData networkFeeResponseData = new NetworkFeeResponseData(networkFeeData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new NetworkFeeResponse(networkFeeResponseData));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public ResponseEntity<BaseResponse> validateNetworkFee(NetworkFeeValidateRequest networkFeeValidateRequest) {
        try {
            FullNodeFeeData fullNodeFeeData = networkFeeValidateRequest.getFullNodeFeeData();
            if (!validateFullNodeFee(fullNodeFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new NetworkFeeResponse(
                                STATUS_ERROR,
                                FULL_NODE_FEE_VALIDATION_ERROR));
            }
            NetworkFeeData networkFeeData = networkFeeValidateRequest.getNetworkFeeData();
            boolean isValid = isNetworkFeeValid(networkFeeData, fullNodeFeeData.getAmount());
            signNetworkFee(networkFeeData, isValid);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new NetworkFeeResponse(new NetworkFeeResponseData(networkFeeData)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private boolean validateFullNodeFee(FullNodeFeeData fullNodeFeeData) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(fullNodeFeeData);
        return BaseTransactionCrypto.FullNodeFeeData.isBaseTransactionValid(new TransactionData(baseTransactions), fullNodeFeeData);
    }

    private boolean isNetworkFeeValid(NetworkFeeData networkFeeData, BigDecimal fullNodeFeeAmount) {

        return networkFeeData.getReducedAmount().equals(networkFeeData.getOriginalAmount().subtract(fullNodeFeeAmount))
                && isNetworkFeeValid(networkFeeData);
    }

    public boolean isNetworkFeeValid(NetworkFeeData networkFeeData) {
        BigDecimal calculatedNetworkFee = calculateNetworkFee(networkFeeData.getReducedAmount());
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

    //TODO: temp implementation in calculating fees
    private BigDecimal calculateNetworkFee(BigDecimal reducedAmount) {
        return reducedAmount.multiply(new BigDecimal("0.01"));
    }

}
