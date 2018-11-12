package io.coti.trustscore.services;
import io.coti.basenode.crypto.BaseTransactionCryptoWrapper;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.trustscore.data.Enums.TransactionName;
import io.coti.trustscore.http.GetFeesBaseTransactionsResponse;
import io.coti.trustscore.http.data.FeeBaseTransactionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class FeesService {

    @Value("${rolling.reserve.address}")
    private String rollingReserveAddress;

    @Value("${network.fee.address}")
    private String networkFeeAddress;

    public ResponseEntity<BaseResponse> getFeesBaseTransactions(FeeBaseTransactionData fullNodeFeeBaseTransactionData) {
        BaseTransactionCryptoWrapper baseFeeTransactionCryptoWrapper = new BaseTransactionCryptoWrapper(fullNodeFeeBaseTransactionData.getBaseTransactionData());
        //if (baseFeeTransactionCryptoWrapper.IsBaseTransactionValid(new Hash(fullNodeFeeBaseTransactionData.getHash())))
        //    throw new IllegalArgumentException("fullnode base transaction fee is not valid {}");

        BaseTransactionData networkBaseTransactionData = new BaseTransactionData(new Hash(networkFeeAddress),
                calculateNetworkFee(fullNodeFeeBaseTransactionData),java.util.Date.from(Instant.now()));
        BaseTransactionCryptoWrapper baseTransactionCryptoWrapper = new BaseTransactionCryptoWrapper(networkBaseTransactionData);
        baseTransactionCryptoWrapper.setBaseTransactionHash();
        BaseTransactionData rollingReserveBaseTransactionData = new BaseTransactionData(new Hash(rollingReserveAddress),
                calculateRollingReserveFee(fullNodeFeeBaseTransactionData),java.util.Date.from(Instant.now()));
        baseTransactionCryptoWrapper = new BaseTransactionCryptoWrapper(rollingReserveBaseTransactionData);
        baseTransactionCryptoWrapper.setBaseTransactionHash();
        return ResponseEntity.status(HttpStatus.OK).body(new GetFeesBaseTransactionsResponse(
                new FeeBaseTransactionData(networkBaseTransactionData,networkBaseTransactionData.getSignatureData(),
                        NodeCryptoHelper.getNodeHash(), fullNodeFeeBaseTransactionData.originalAmount, TransactionName.NETWORK_FEE_PAYMENT),
                new FeeBaseTransactionData(rollingReserveBaseTransactionData,rollingReserveBaseTransactionData.getSignatureData(),
                        NodeCryptoHelper.getNodeHash(), fullNodeFeeBaseTransactionData.originalAmount, TransactionName.ROLLING_RESERVE_PAYMENT)));
    }


    private BigDecimal calculateNetworkFee(FeeBaseTransactionData fullNodeFeeBaseTransactionData){
        return fullNodeFeeBaseTransactionData.getAmount().multiply(new BigDecimal("0.01"));
    }


    private BigDecimal calculateRollingReserveFee(FeeBaseTransactionData fullNodeFeeBaseTransactionData){
        return fullNodeFeeBaseTransactionData.getAmount().multiply(new BigDecimal("0.02"));
    }
}
