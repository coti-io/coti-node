package io.coti.trustscore.services;
import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.http.*;
import io.coti.trustscore.http.data.NetworkFeeResponseData;
import io.coti.trustscore.http.data.RollingReserveResponseData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FeesService {

    @Value("${rolling.reserve.address}")
    private Hash rollingReserveAddress;

    @Value("${network.fee.address}")
    private Hash networkFeeAddress;



    @Value("${network.fee.difference.validation}")
    private BigDecimal networkFeeDifferenceValidation;

    @Value("${rolling.reserve.difference.validation}")
    private BigDecimal rollingReserveDifferenceValidation;


    public ResponseEntity<BaseResponse> createNetworkFee(NetworkFeeRequest networkFeeRequest) {
        try {
            BigDecimal originalAmount = networkFeeRequest.getFullNodeFeeData().getOriginalAmount();
            BigDecimal fee = calculateNetworkFee(networkFeeRequest.getFullNodeFeeData().getOriginalAmount());
            NetworkFeeData networkFeeData = new NetworkFeeData(networkFeeAddress,fee, originalAmount, new Date());
            setNetworkFeeHash(networkFeeData);
            signNetworkFee(networkFeeData);
            NetworkFeeResponseData networkFeeResponseData = new NetworkFeeResponseData(networkFeeData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new NetworkFeeResponse(networkFeeResponseData));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public ResponseEntity<BaseResponse> createRollingReserveFee(RollingReserveRequest rollingReserveRequest) {
        try {
            BigDecimal originalAmount = rollingReserveRequest.getFullNodeFeeData().getOriginalAmount();
            BigDecimal fee = calculateRollingReserveFee(rollingReserveRequest.getFullNodeFeeData().getOriginalAmount());
            RollingReserveData rollingReserveData = new RollingReserveData(rollingReserveAddress,fee, originalAmount, new Date());
            setRollingReserveNodeFeeHash(rollingReserveData);
            signRollingReserveFee(rollingReserveData);
            RollingReserveResponseData rollingReserveResponseData = new RollingReserveResponseData(rollingReserveData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RollingReserveResponse(rollingReserveResponseData));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public void setRollingReserveNodeFeeHash(RollingReserveData rollingReserveData) {
        BaseTransactionCrypto.RollingReserveData.setBaseTransactionHash(rollingReserveData);
    }

    public void setNetworkFeeHash(NetworkFeeData networkFeeData) {
        BaseTransactionCrypto.NetworkFeeData.setBaseTransactionHash(networkFeeData);
    }


    public void signNetworkFee(NetworkFeeData networkFeeData) throws ClassNotFoundException {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(networkFeeData);
        BaseTransactionCrypto.NetworkFeeData.signMessage(new TransactionData(baseTransactions), networkFeeData);
    }


    public void signRollingReserveFee(RollingReserveData rollingReserveData) throws ClassNotFoundException {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(rollingReserveData);
        BaseTransactionCrypto.RollingReserveData.signMessage(new TransactionData(baseTransactions), rollingReserveData);
    }




    public ResponseEntity<BaseResponse> validateNetworkFee(NetworkFeeValidateRequest networkFeeValidateRequest) {
        NetworkFeeData networkFeeData =  networkFeeValidateRequest.getNetworkFeeData();

        boolean isValid = isNetworkFeeValid(networkFeeData);

        ByteBuffer signBuffer = ByteBuffer.allocate(networkFeeData.getHash().getBytes().length + 1);
        signBuffer.put(networkFeeData.getHash().getBytes());
        signBuffer.put((byte) (isValid ? 1 : 0));

        networkFeeData.getTrustScoreNodeResult().add(new TrustScoreNodeResultData(NodeCryptoHelper.getNodeHash(),
                NodeCryptoHelper.signMessage(signBuffer.array()),isValid ));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new NetworkFeeResponse(new NetworkFeeResponseData(networkFeeData)));
    }


    public ResponseEntity<BaseResponse> validateRollingReserve(RollingReserveValidateRequest rollingReserveValidateRequest) {

        RollingReserveData rollingReserveData =  rollingReserveValidateRequest.getRollingReserveData();

        boolean isValid = isRollingReserveValid(rollingReserveData);

        ByteBuffer signBuffer = ByteBuffer.allocate(rollingReserveData.getHash().getBytes().length + 1);
        signBuffer.put(rollingReserveData.getHash().getBytes());
        signBuffer.put((byte) (isValid ? 1 : 0));

        rollingReserveData.getTrustScoreNodeResult().add(new TrustScoreNodeResultData(NodeCryptoHelper.getNodeHash(),
                NodeCryptoHelper.signMessage(signBuffer.array()),isValid ));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new RollingReserveResponse(new RollingReserveResponseData(rollingReserveData)));
    }



    private boolean isNetworkFeeValid(NetworkFeeData networkFeeData){
        BigDecimal calculatedReserve =  calculateRollingReserveFee(networkFeeData.getOriginalAmount());
        int compareResult =  networkFeeDifferenceValidation.compareTo(calculatedReserve.subtract(networkFeeData.getAmount()).abs());
        return  compareResult >= 0;
    }


    private boolean isRollingReserveValid(RollingReserveData rollingReserveData){
        BigDecimal calculatedReserve =  calculateRollingReserveFee(rollingReserveData.getOriginalAmount());
        int compareResult =  rollingReserveDifferenceValidation.compareTo(calculatedReserve.subtract(rollingReserveData.getAmount()).abs());
        return  compareResult >= 0;
    }


    //TODO: temp implementation in calculating fees
    private BigDecimal calculateNetworkFee(BigDecimal originalAmount){
        return originalAmount.multiply(new BigDecimal("0.01"));
    }


    private BigDecimal calculateRollingReserveFee(BigDecimal originalAmount){
        return originalAmount.multiply(new BigDecimal("0.02"));
    }
}
