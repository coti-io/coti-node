package io.coti.fullnode.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.FullNodeFeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.IValidationService;
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
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.basenode.services.BaseNodeTransactionHelper.CURRENCY_SCALE;

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
    private FullNodeFeeRequestCrypto fullNodeFeeRequestCrypto;
    @Autowired
    private IValidationService validationService;

    public ResponseEntity<IResponse> createFullNodeFee(FullNodeFeeRequest fullNodeFeeRequest) {
        try {
            if (!fullNodeFeeRequestCrypto.verifySignature(fullNodeFeeRequest)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            boolean feeIncluded = fullNodeFeeRequest.isFeeIncluded();
            BigDecimal originalAmount = fullNodeFeeRequest.getOriginalAmount().stripTrailingZeros();
            if (!validationService.validateAmountField(originalAmount)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_AMOUNT, STATUS_ERROR));
            }
            Hash address = this.getAddress();
            BigDecimal amount;
            if (zeroFeeUserHashes.contains(fullNodeFeeRequest.getUserHash().toString())) {
                amount = new BigDecimal(0);
            } else {
                BigDecimal fee = originalAmount.multiply(feePercentage).divide(new BigDecimal(100));
                if (fee.compareTo(minimumFee) <= 0) {
                    amount = minimumFee;
                } else if (fee.compareTo(maximumFee) >= 0) {
                    amount = maximumFee;
                } else {
                    amount = fee;
                }
            }

            if (amount.scale() > CURRENCY_SCALE) {
                amount = amount.setScale(CURRENCY_SCALE, RoundingMode.DOWN);
            }
            if (amount.scale() > 0) {
                amount = amount.stripTrailingZeros();
            }
            if (feeIncluded && originalAmount.compareTo(amount) <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(String.format(INVALID_AMOUNT_VS_FULL_NODE_FEE, amount.toPlainString()), STATUS_ERROR));
            }

            FullNodeFeeData fullNodeFeeData = new FullNodeFeeData(address, amount, originalAmount, Instant.now());
            setFullNodeFeeHash(fullNodeFeeData);
            signFullNodeFee(fullNodeFeeData);
            FullNodeFeeResponseData fullNodeFeeResponseData = new FullNodeFeeResponseData(fullNodeFeeData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new FullNodeFeeResponse(fullNodeFeeResponseData));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    public Hash getAddress() {
        return NodeCryptoHelper.generateAddress(seed, FULL_NODE_FEE_ADDRESS_INDEX);
    }

    public void setFullNodeFeeHash(FullNodeFeeData fullNodeFeeData) {
        BaseTransactionCrypto.FULL_NODE_FEE_DATA.createAndSetBaseTransactionHash(fullNodeFeeData);
    }

    public void signFullNodeFee(FullNodeFeeData fullNodeFeeData) {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(fullNodeFeeData);
        BaseTransactionCrypto.FULL_NODE_FEE_DATA.signMessage(new TransactionData(baseTransactions), fullNodeFeeData, FULL_NODE_FEE_ADDRESS_INDEX);
    }

    public boolean validateFeeData(FullNodeFeeData fullNodeFeeData) {
        return fullNodeFeeData.getAddressHash().equals(this.getAddress());
    }
}
