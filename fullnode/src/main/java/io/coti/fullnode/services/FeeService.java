package io.coti.fullnode.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.fullnode.crypto.FullNodeFeeRequestCrypto;
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

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class FeeService {
    private static final int FULL_NODE_FEE_ADDRESS_INDEX = 0;
    @Value("#{'${zero.fee.user.hashes}'.split(',')}")
    private List<String> zeroFeeUserHashes;
    @Value("${minimumFee}")
    private BigDecimal minimumFee;
    @Value("${maximumFee}")
    private BigDecimal maximumFee;
    @Value("${fee.percentage}")
    private BigDecimal feePercentage;
    @Value("${fullnode.seed}")
    private String seed;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private FullNodeFeeRequestCrypto fullNodeFeeRequestCrypto;

    public ResponseEntity<BaseResponse> createFullNodeFee(FullNodeFeeRequest fullNodeFeeRequest) {
        try {
            if (!fullNodeFeeRequestCrypto.verifySignature(fullNodeFeeRequest)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            BigDecimal originalAmount = fullNodeFeeRequest.originalAmount;
            Hash address = this.getAddress();
            BigDecimal amount;
            if (zeroFeeUserHashes.contains(fullNodeFeeRequest.getUserHash().toString())) {
                amount = new BigDecimal(0);
            } else {
                BigDecimal fee = originalAmount.multiply(feePercentage).divide(new BigDecimal(100));
                amount = fee.compareTo(minimumFee) <= 0 ? minimumFee : fee.compareTo(maximumFee) >= 0 ? maximumFee : fee;
            }

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
        return nodeCryptoHelper.generateAddress(seed, FULL_NODE_FEE_ADDRESS_INDEX);
    }

    public void setFullNodeFeeHash(FullNodeFeeData fullNodeFeeData) throws ClassNotFoundException {
        BaseTransactionCrypto.FullNodeFeeData.setBaseTransactionHash(fullNodeFeeData);
    }

    public void signFullNodeFee(FullNodeFeeData fullNodeFeeData) throws ClassNotFoundException {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(fullNodeFeeData);
        BaseTransactionCrypto.FullNodeFeeData.signMessage(new TransactionData(baseTransactions), fullNodeFeeData, FULL_NODE_FEE_ADDRESS_INDEX);
    }

    public boolean validateFeeData(FullNodeFeeData fullNodeFeeData) {
        return fullNodeFeeData.getAddressHash().equals(this.getAddress());
    }
}
