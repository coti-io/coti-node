package io.coti.trustscore.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.trustscore.http.*;
import io.coti.trustscore.http.data.NetworkFeeResponseData;
import io.coti.trustscore.http.data.RollingReserveResponseData;
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

    @Value("${rolling.reserve.address}")
    private Hash rollingReserveAddress;

    @Value("${network.fee.address}")
    private Hash networkFeeAddress;


    @Value("${network.fee.difference.validation}")
    private BigDecimal networkFeeDifferenceValidation;

    @Value("${rolling.reserve.difference.validation}")
    private BigDecimal rollingReserveDifferenceValidation;


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
            BigDecimal fee = calculateNetworkFee(originalAmount);
            NetworkFeeData networkFeeData = new NetworkFeeData(networkFeeAddress, fee, originalAmount, new Date());
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


    public ResponseEntity<Response> createRollingReserveFee(RollingReserveRequest rollingReserveRequest) {
        try {
            FullNodeFeeData fullNodeFeeData = rollingReserveRequest.getFullNodeFeeData();
            if (!validateFullNodeFee(fullNodeFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new RollingReserveResponse(
                                STATUS_ERROR,
                                FULL_NODE_FEE_VALIDATION_ERROR));
            }
            BigDecimal originalAmount = fullNodeFeeData.getOriginalAmount();
            BigDecimal fee = calculateRollingReserveFee(rollingReserveRequest.getFullNodeFeeData().getOriginalAmount());
            RollingReserveData rollingReserveData = new RollingReserveData(rollingReserveAddress, fee, originalAmount, new Date());
            setRollingReserveNodeFeeHash(rollingReserveData);
            signRollingReserveFee(rollingReserveData, true);
            RollingReserveResponseData rollingReserveResponseData = new RollingReserveResponseData(rollingReserveData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RollingReserveResponse(rollingReserveResponseData));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean validateFullNodeFee(FullNodeFeeData fullNodeFeeData) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(fullNodeFeeData);
        return BaseTransactionCrypto.FullNodeFeeData.isBaseTransactionValid(new TransactionData(baseTransactions), fullNodeFeeData);
    }

    public void setRollingReserveNodeFeeHash(RollingReserveData rollingReserveData) throws ClassNotFoundException {
        BaseTransactionCrypto.RollingReserveData.setBaseTransactionHash(rollingReserveData);
    }

    public void setNetworkFeeHash(NetworkFeeData networkFeeData) throws ClassNotFoundException {
        BaseTransactionCrypto.NetworkFeeData.setBaseTransactionHash(networkFeeData);
    }


    public void signNetworkFee(NetworkFeeData networkFeeData, boolean isValid) throws ClassNotFoundException {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(networkFeeData);
        BaseTransactionCrypto.NetworkFeeData.signMessage(new TransactionData(baseTransactions), networkFeeData, new TrustScoreNodeResultData(NodeCryptoHelper.getNodeHash(), isValid));
    }


    public void signRollingReserveFee(RollingReserveData rollingReserveData, boolean isValid) throws ClassNotFoundException {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(rollingReserveData);
        BaseTransactionCrypto.RollingReserveData.signMessage(new TransactionData(baseTransactions), rollingReserveData, new TrustScoreNodeResultData(NodeCryptoHelper.getNodeHash(), isValid));
    }


    public ResponseEntity<BaseResponse> validateNetworkFee(NetworkFeeValidateRequest networkFeeValidateRequest) {
        try {
            NetworkFeeData networkFeeData = networkFeeValidateRequest.getNetworkFeeData();
            boolean isValid = isNetworkFeeValid(networkFeeData);
            signNetworkFee(networkFeeData, isValid);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new NetworkFeeResponse(new NetworkFeeResponseData(networkFeeData)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public ResponseEntity<BaseResponse> validateRollingReserve(RollingReserveValidateRequest rollingReserveValidateRequest) {
        try {
            RollingReserveData rollingReserveData = rollingReserveValidateRequest.getRollingReserveData();
            boolean isValid = isRollingReserveValid(rollingReserveData);
            signRollingReserveFee(rollingReserveData, isValid);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RollingReserveResponse(new RollingReserveResponseData(rollingReserveData)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private boolean isNetworkFeeValid(NetworkFeeData networkFeeData) {
        BigDecimal calculatedReserve = calculateNetworkFee(networkFeeData.getOriginalAmount());
        int compareResult = networkFeeDifferenceValidation.compareTo(calculatedReserve.subtract(networkFeeData.getAmount()).abs());
        return compareResult >= 0;
    }


    private boolean isRollingReserveValid(RollingReserveData rollingReserveData) {
        BigDecimal calculatedReserve = calculateRollingReserveFee(rollingReserveData.getOriginalAmount());
        int compareResult = rollingReserveDifferenceValidation.compareTo(calculatedReserve.subtract(rollingReserveData.getAmount()).abs());
        return compareResult >= 0;
    }


    //TODO: temp implementation in calculating fees
    private BigDecimal calculateNetworkFee(BigDecimal originalAmount) {
        return originalAmount.multiply(new BigDecimal("0.01"));
    }


    private BigDecimal calculateRollingReserveFee(BigDecimal originalAmount) {
        return originalAmount.multiply(new BigDecimal("0.02"));
    }
}
