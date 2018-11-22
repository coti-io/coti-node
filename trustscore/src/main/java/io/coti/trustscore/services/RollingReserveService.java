package io.coti.trustscore.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.trustscore.http.RollingReserveRequest;
import io.coti.trustscore.http.RollingReserveResponse;
import io.coti.trustscore.http.RollingReserveValidateRequest;
import io.coti.trustscore.http.data.RollingReserveResponseData;
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

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.trustscore.http.HttpStringConstants.FULL_NODE_FEE_VALIDATION_ERROR;

@Slf4j
@Service
public class RollingReserveService {
    @Value("${rolling.reserve.address}")
    private Hash rollingReserveAddress;
    @Value("${rolling.reserve.difference.validation}")
    private BigDecimal rollingReserveDifferenceValidation;
    @Autowired
    private FeeService feeService;

    public ResponseEntity<Response> createRollingReserveFee(RollingReserveRequest rollingReserveRequest) {
        try {
            NetworkFeeData networkFeeData = rollingReserveRequest.getNetworkFeeData();
            if (!feeService.validateNetworkFee(networkFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new RollingReserveResponse(
                                STATUS_ERROR,
                                FULL_NODE_FEE_VALIDATION_ERROR));
            }
            BigDecimal originalAmount = networkFeeData.getOriginalAmount();
            BigDecimal reducedAmount = networkFeeData.getReducedAmount().subtract(networkFeeData.getAmount());
            BigDecimal rollingReserveAmount = calculateRollingReserveAmount(reducedAmount);
            RollingReserveData rollingReserveData = new RollingReserveData(rollingReserveAddress, rollingReserveAmount, originalAmount, reducedAmount, new Date());
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

    public ResponseEntity<BaseResponse> validateRollingReserve(RollingReserveValidateRequest rollingReserveValidateRequest) {
        try {
            NetworkFeeData networkFeeData = rollingReserveValidateRequest.getNetworkFeeData();
            if (!feeService.validateNetworkFee(networkFeeData)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(new RollingReserveResponse(
                                STATUS_ERROR,
                                FULL_NODE_FEE_VALIDATION_ERROR));
            }
            RollingReserveData rollingReserveData = rollingReserveValidateRequest.getRollingReserveData();
            boolean isValid = isRollingReserveValid(rollingReserveData, networkFeeData);
            signRollingReserveFee(rollingReserveData, isValid);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new RollingReserveResponse(new RollingReserveResponseData(rollingReserveData)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setRollingReserveNodeFeeHash(RollingReserveData rollingReserveData) throws ClassNotFoundException {
        BaseTransactionCrypto.RollingReserveData.setBaseTransactionHash(rollingReserveData);
    }

    public void signRollingReserveFee(RollingReserveData rollingReserveData, boolean isValid) throws ClassNotFoundException {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(rollingReserveData);
        BaseTransactionCrypto.RollingReserveData.signMessage(new TransactionData(baseTransactions), rollingReserveData, new TrustScoreNodeResultData(NodeCryptoHelper.getNodeHash(), isValid));
    }

    private boolean isRollingReserveValid(RollingReserveData rollingReserveData, NetworkFeeData networkFeeData) {
        return rollingReserveData.getReducedAmount().equals(networkFeeData.getReducedAmount().subtract(networkFeeData.getAmount()))
                && isRollingReserveValid(rollingReserveData);
    }

    private boolean isRollingReserveValid(RollingReserveData rollingReserveData) {
        BigDecimal calculatedReserve = calculateRollingReserveAmount(rollingReserveData.getReducedAmount());
        int compareResult = rollingReserveDifferenceValidation.compareTo(calculatedReserve.subtract(rollingReserveData.getAmount()).abs());
        return compareResult >= 0 && validateRollingReserveCrypto(rollingReserveData);
    }

    private boolean validateRollingReserveCrypto(RollingReserveData rollingReserveData) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(rollingReserveData);
        return BaseTransactionCrypto.RollingReserveData.isBaseTransactionValid(new TransactionData(baseTransactions), rollingReserveData);
    }

    private BigDecimal calculateRollingReserveAmount(BigDecimal reducedAmount) {
        return reducedAmount.multiply(new BigDecimal("0.02"));
    }
}
