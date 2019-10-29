package io.coti.financialserver.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TokenServiceFeeData;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.financialserver.data.ReservedAddress;
import io.coti.financialserver.http.GenerateTokenFeeRequest;
import io.coti.financialserver.http.TokenGenerationFeeResponse;
import io.coti.financialserver.http.data.TokenGenerationFeeResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class FeeService {

    @Value("${token.generation.fee}")
    private BigDecimal tokenGenerationFee;
    @Value("${financialserver.seed}")
    private String seed;
    @Value("${global.private.key}")
    private String privateKey;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private ICurrencyService currencyService;

    public ResponseEntity<BaseResponse> createTokenGenerationFee(GenerateTokenFeeRequest generateTokenFeeRequest) {
        try {
            Hash nativeCurrencyHash = currencyService.getNativeCurrencyHash();
            BigDecimal amount = tokenGenerationFee;
            Hash address = networkFeeAddress();

            TokenServiceFeeData tokenServiceFeeData = new TokenServiceFeeData(address, nativeCurrencyHash, nodeCryptoHelper.getNodeHash(), amount, Instant.now());
            setFeeHash(tokenServiceFeeData);
            signTokenGenerationFee(tokenServiceFeeData);
            TokenGenerationFeeResponseData tokenGenerationFeeResponseData = new TokenGenerationFeeResponseData(tokenServiceFeeData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new TokenGenerationFeeResponse(tokenGenerationFeeResponseData));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    public Hash networkFeeAddress() {
        return nodeCryptoHelper.generateAddress(seed, (int) ReservedAddress.NETWORK_FEE_POOL.getIndex());
    }

    public void setFeeHash(TokenServiceFeeData tokenServiceFeeData) throws ClassNotFoundException {
        BaseTransactionCrypto.TokenServiceFeeData.setBaseTransactionHash(tokenServiceFeeData);
    }

    public void signTokenGenerationFee(TokenServiceFeeData tokenServiceFeeData) {
        tokenServiceFeeData.setSignature(nodeCryptoHelper.signMessage(tokenServiceFeeData.getHash().getBytes()));
    }
}
