package io.coti.fullnode.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.fullnode.http.FullNodeFeeRequest;
import io.coti.fullnode.http.FullNodeFeeResponse;
import io.coti.fullnode.http.data.FullNodeFeeResponseData;
import lombok.extern.slf4j.Slf4j;
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
import static io.coti.fullnode.services.NodeServiceManager.*;

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
    @Value("${fullnode.seed.key:}")
    private String seed;
    @Value("${regular.token.fullnode.fee}")
    private BigDecimal regularTokenFullnodeFee;
    @Value("${secret.fullnode.seed.name.key:}")
    private String seedSecretName;

    public void init() {
        seed = secretManagerService.getSecret(seed, seedSecretName, "seed");
    }

    public ResponseEntity<IResponse> createFullNodeFee(FullNodeFeeRequest fullNodeFeeRequest) {
        try {
            if (!currencyService.isCurrencyHashAllowed(fullNodeFeeRequest.getOriginalCurrencyHash())) {
                return ResponseEntity.badRequest().body(new Response(MULTI_DAG_IS_NOT_SUPPORTED, STATUS_ERROR));
            }

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
            Hash originalCurrencyHash = fullNodeFeeRequest.getOriginalCurrencyHash();
            if (zeroFeeUserHashes.contains(fullNodeFeeRequest.getUserHash().toString())) {
                amount = new BigDecimal(0);
            } else {
                if (currencyService.isNativeCurrency(originalCurrencyHash)) {
                    BigDecimal fee = originalAmount.multiply(feePercentage).divide(new BigDecimal(100));
                    if (fee.compareTo(minimumFee) <= 0) {
                        amount = minimumFee;
                    } else if (fee.compareTo(maximumFee) >= 0) {
                        amount = maximumFee;
                    } else {
                        amount = fee;
                    }
                } else {
                    CurrencyData currencyData = currencies.getByHash(fullNodeFeeRequest.getOriginalCurrencyHash());
                    if (currencyData != null && currencyData.getCurrencyTypeData().getCurrencyType() == CurrencyType.REGULAR_CMD_TOKEN) {
                        amount = regularTokenFullnodeFee;
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(String.format(UNDEFINED_TOKEN_TYPE_FEE, fullNodeFeeRequest.getOriginalCurrencyHash()), STATUS_ERROR));
                    }
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

            Hash feeDataCurrencyHash = originalCurrencyHash == null ? null : new Hash(currencyService.getNativeCurrencyHash().toString());
            FullNodeFeeData fullNodeFeeData = new FullNodeFeeData(address, feeDataCurrencyHash, amount, originalCurrencyHash, originalAmount, Instant.now());
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
        return nodeIdentityService.generateAddress(seed, FULL_NODE_FEE_ADDRESS_INDEX);
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
