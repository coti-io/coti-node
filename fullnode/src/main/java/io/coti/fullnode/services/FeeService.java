package io.coti.fullnode.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import io.coti.fullnode.http.FullNodeFeeRequest;
import io.coti.fullnode.http.FullNodeFeeResponse;
import io.coti.fullnode.http.data.FullNodeFeeResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Service
public class FeeService {
    @Value("${minimumFee}")
    private BigDecimal minimumFee;
    @Value("${maximumFee}")
    private BigDecimal maximumFee;
    @Value("${feePercentage}")
    private BigDecimal feePercentage;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    public ResponseEntity<BaseResponse> createFullNodeFee(FullNodeFeeRequest fullNodeFeeRequest) {
        BigDecimal originalAmount = fullNodeFeeRequest.originalAmount;
        Hash address = this.getAddress();
        BigDecimal amount = originalAmount.compareTo(minimumFee) <= 0 ? minimumFee : originalAmount.compareTo(maximumFee) >= 0 ? maximumFee : originalAmount.multiply(feePercentage).divide(new BigDecimal(100));
        FullNodeFeeData fullNodeFeeData = new FullNodeFeeData(address, amount, originalAmount, new Date());
        setFullNodeFeeHash(fullNodeFeeData);
        signFullNodeFee(fullNodeFeeData);
        FullNodeFeeResponseData fullNodeFeeResponseData = new FullNodeFeeResponseData(fullNodeFeeData);
        FullNodeFeeResponse fullNodeFeeResponse = new FullNodeFeeResponse(fullNodeFeeResponseData);
        return ResponseEntity.status(HttpStatus.OK)
                .body(fullNodeFeeResponse);
    }

    public Hash getAddress() {
        return nodeCryptoHelper.getNodeAddress();
    }

    public void setFullNodeFeeHash(FullNodeFeeData fullNodeFeeData) {
        BaseTransactionCrypto.FullNodeFeeData.setBaseTransactionHash(fullNodeFeeData);
    }

    public void signFullNodeFee(FullNodeFeeData fullNodeFeeData) {
        BaseTransactionCrypto.FullNodeFeeData.signMessage(fullNodeFeeData);
    }
}
