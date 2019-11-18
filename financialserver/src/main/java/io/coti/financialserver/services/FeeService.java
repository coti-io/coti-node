package io.coti.financialserver.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TokenServiceFeeData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.financialserver.data.ReservedAddress;
import io.coti.financialserver.http.GenerateTokenFeeRequest;
import io.coti.financialserver.http.TokenFeeResponse;
import io.coti.financialserver.http.data.TokenServiceFeeResponseData;
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

    private final static BigDecimal totalSupplyFeeFactor = BigDecimal.ZERO;
    @Value("${token.generation.fee}")
    private BigDecimal tokenGenerationFee;
    @Value("${financialserver.seed}")
    private String seed;
    @Value("${global.private.key}")
    private String privateKey;
    @Value("${regular.token.minting.fee}")
    private BigDecimal regularTokenMintingFee;

    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;
    @Autowired
    private ICurrencyService currencyService;
    @Autowired
    private CurrencyRateService currencyRateService;

    public ResponseEntity<IResponse> createTokenGenerationFee(GenerateTokenFeeRequest generateTokenRequest, Hash currencyHash) {
        try {
            TokenServiceFeeData tokenServiceFeeData = new TokenServiceFeeData(networkFeeAddress(), currencyService.getNativeCurrencyHash(),
                    NodeCryptoHelper.getNodeHash(), calculateTokenGenerationFee(generateTokenRequest.getCurrencyData().getTotalSupply()),
                    currencyHash, generateTokenRequest.getCurrencyData().getTotalSupply(), Instant.now());
            setFeeHash(tokenServiceFeeData);
            signTokenGenerationFee(tokenServiceFeeData);
            TokenServiceFeeResponseData tokenServiceFeeResponseData = new TokenServiceFeeResponseData(tokenServiceFeeData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new TokenFeeResponse(tokenServiceFeeResponseData));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    public BigDecimal calculateTokenGenerationFee(BigDecimal totalSupply) {
        return totalSupplyFeeFactor.multiply(totalSupply).add(tokenGenerationFee);
    }

    public BigDecimal calculateTokenMintingFee(BigDecimal amount, Instant creationTime, CurrencyData currencyData) {
        return regularTokenMintingFee;
//        return tokenMintingFeeFactor.divide(new BigDecimal(100)).multiply(amount)
//                .multiply(new BigDecimal(currencyRateService.getTokenRateToNativeCoin(currencyData))).add(tokenMintingMinimumFee);
    }

    protected Hash networkFeeAddress() {
        return nodeCryptoHelper.generateAddress(seed, (int) ReservedAddress.NETWORK_FEE_POOL.getIndex());
    }

    protected void setFeeHash(TokenServiceFeeData tokenServiceFeeData) throws ClassNotFoundException {
        BaseTransactionCrypto.TokenServiceFeeData.setBaseTransactionHash(tokenServiceFeeData);
    }

    protected void signTokenGenerationFee(TokenServiceFeeData tokenServiceFeeData) {
        tokenServiceFeeData.setSignature(nodeCryptoHelper.signMessage(tokenServiceFeeData.getHash().getBytes()));
    }
}
