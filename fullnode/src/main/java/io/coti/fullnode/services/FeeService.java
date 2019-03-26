package io.coti.fullnode.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FeeService {
    @Value("${minimumFee}")
    private BigDecimal minimumFee;
    @Value("${maximumFee}")
    private BigDecimal maximumFee;
    @Value("${fee.percentage}")
    private BigDecimal feePercentage;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    public ResponseEntity<BaseResponse> createFullNodeFee(FullNodeFeeRequest fullNodeFeeRequest) {
        try {
            BigDecimal originalAmount = fullNodeFeeRequest.originalAmount;
            Hash address = this.getAddress();
            BigDecimal fee = originalAmount.multiply(feePercentage).divide(new BigDecimal(100));
            BigDecimal amount = fee.compareTo(minimumFee) <= 0 ? minimumFee : fee.compareTo(maximumFee) >= 0 ? maximumFee : fee;
            FullNodeFeeData fullNodeFeeData = new FullNodeFeeData(address, amount, originalAmount, Instant.now());
            setFullNodeFeeHash(fullNodeFeeData);
            signFullNodeFee(fullNodeFeeData);
            FullNodeFeeResponseData fullNodeFeeResponseData = new FullNodeFeeResponseData(fullNodeFeeData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new FullNodeFeeResponse(fullNodeFeeResponseData));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Hash getAddress() {
        return nodeCryptoHelper.getNodeAddress();
    }

    public void setFullNodeFeeHash(FullNodeFeeData fullNodeFeeData) throws ClassNotFoundException {
        BaseTransactionCrypto.FullNodeFeeData.setBaseTransactionHash(fullNodeFeeData);
    }

    public void signFullNodeFee(FullNodeFeeData fullNodeFeeData) throws ClassNotFoundException {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(fullNodeFeeData);
        BaseTransactionCrypto.FullNodeFeeData.signMessage(new TransactionData(baseTransactions), fullNodeFeeData);
    }
}
