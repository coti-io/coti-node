package io.coti.financialserver.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TokenGenerationData;
import io.coti.basenode.data.TokenGenerationFeeBaseTransactionData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
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
            BigDecimal tokenGenerationFee = calculateTokenGenerationFee(generateTokenRequest.getCurrencyData().getTotalSupply());
            TokenGenerationData tokenGenerationData = new TokenGenerationData(generateTokenRequest.getCurrencyData().getName(),
                    generateTokenRequest.getCurrencyData().getSymbol(), currencyHash, generateTokenRequest.getCurrencyData().getTotalSupply(),
                    generateTokenRequest.getCurrencyData().getScale(), Instant.now(), tokenGenerationFee);
            TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData =
                    new TokenGenerationFeeBaseTransactionData(networkFeeAddress(), currencyService.getNativeCurrencyHash(),
                            NodeCryptoHelper.getNodeHash(), tokenGenerationFee, Instant.now(), tokenGenerationData);
            setTokenGenerationFeeHash(tokenGenerationFeeBaseTransactionData);
            signTokenGenerationFee(tokenGenerationFeeBaseTransactionData);
            TokenGenerationFeeResponseData tokenGenerationFeeResponseData = new TokenGenerationFeeResponseData(tokenGenerationFeeBaseTransactionData);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new TokenGenerationFeeResponse(tokenGenerationFeeResponseData));
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

    protected void setTokenGenerationFeeHash(TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData) {
        BaseTransactionCrypto.TOKEN_GENERATION_FEE_BASE_TRANSACTION_DATA.createAndSetBaseTransactionHash(tokenGenerationFeeBaseTransactionData);
    }

    protected void signTokenGenerationFee(TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData) {
        tokenGenerationFeeBaseTransactionData.setSignature(NodeCryptoHelper.signMessage(tokenGenerationFeeBaseTransactionData.getHash().getBytes()));
    }
}
