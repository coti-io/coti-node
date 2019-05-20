package io.coti.fullnode.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.fullnode.http.FullNodeFeeRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class FullNodeFeeRequestCrypto extends SignatureValidationCrypto<FullNodeFeeRequest> {
    @Override
    public byte[] getSignatureMessage(FullNodeFeeRequest fullNodeFeeRequest) {
        String decimalOriginalAmountRepresentation = fullNodeFeeRequest.getOriginalAmount().stripTrailingZeros().toPlainString();
        byte[] originalAmountInBytes = decimalOriginalAmountRepresentation.getBytes(StandardCharsets.UTF_8);
        return CryptoHelper.cryptoHash(originalAmountInBytes).getBytes();
    }
}
