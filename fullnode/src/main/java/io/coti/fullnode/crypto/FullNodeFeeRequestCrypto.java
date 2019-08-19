package io.coti.fullnode.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.fullnode.http.FullNodeFeeRequest;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class FullNodeFeeRequestCrypto extends SignatureValidationCrypto<FullNodeFeeRequest> {

    @Override
    public byte[] getSignatureMessage(FullNodeFeeRequest fullNodeFeeRequest) {
        byte[] originalCurrencyHashInBytes = fullNodeFeeRequest.getOriginalCurrencyHash() != null ? fullNodeFeeRequest.getOriginalCurrencyHash().getBytes() : new byte[0];
        String decimalOriginalAmountRepresentation = fullNodeFeeRequest.getOriginalAmount().stripTrailingZeros().toPlainString();
        byte[] originalAmountInBytes = decimalOriginalAmountRepresentation.getBytes(StandardCharsets.UTF_8);

        ByteBuffer fullNodeFeeBuffer = ByteBuffer.allocate(originalCurrencyHashInBytes.length + originalAmountInBytes.length)
                .put(originalAmountInBytes).put(originalAmountInBytes);
        return CryptoHelper.cryptoHash(fullNodeFeeBuffer.array()).getBytes();
    }
}
