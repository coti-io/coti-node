package io.coti.financialserver.services;

import io.coti.basenode.crypto.BaseTransactionCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeFeeType;
import io.coti.basenode.data.TokenGenerationFeeBaseTransactionData;
import io.coti.basenode.data.TokenGenerationServiceData;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.INodeFeesService;
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

    @Value("${financialserver.seed.key}")
    private String seed;
    @Autowired
    private ICurrencyService currencyService;
    @Autowired
    private INodeFeesService nodeFeesService;

    public ResponseEntity<IResponse> createTokenGenerationFee(GenerateTokenFeeRequest generateTokenRequest) {
        try {
            BigDecimal tokenGenerationFeeCalculated = calculateTokenGenerationFee(generateTokenRequest.getOriginatorCurrencyData().getTotalSupply());
            TokenGenerationServiceData tokenGenerationServiceData = new TokenGenerationServiceData(generateTokenRequest.getOriginatorCurrencyData(), generateTokenRequest.getCurrencyTypeData(), tokenGenerationFeeCalculated);
            TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData =
                    new TokenGenerationFeeBaseTransactionData(networkFeeAddress(), currencyService.getNativeCurrencyHash(),
                            NodeCryptoHelper.getNodeHash(), tokenGenerationFeeCalculated, Instant.now(), tokenGenerationServiceData);
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
        return nodeFeesService.calculateClassicFee(NodeFeeType.TOKEN_GENERATION_FEE, totalSupply);
    }

    public BigDecimal calculateTokenMintingFee(BigDecimal amount) {
        return nodeFeesService.calculateClassicFee(NodeFeeType.TOKEN_MINTING_FEE, amount);
    }

    protected Hash networkFeeAddress() {
        return NodeCryptoHelper.generateAddress(seed, (int) ReservedAddress.NETWORK_FEE_POOL.getIndex());
    }

    protected void setTokenGenerationFeeHash(TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData) {
        BaseTransactionCrypto.TOKEN_GENERATION_BASE_TRANSACTION_DATA.createAndSetBaseTransactionHash(tokenGenerationFeeBaseTransactionData);
    }

    protected void signTokenGenerationFee(TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData) {
        tokenGenerationFeeBaseTransactionData.setSignature(NodeCryptoHelper.signMessage(tokenGenerationFeeBaseTransactionData.getHash().getBytes()));
    }
}
