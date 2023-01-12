package io.coti.fullnode.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.basenode.http.RepropagateTransactionRequest;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class ResendTransactionRequestCrypto extends SignatureValidationCrypto<RepropagateTransactionRequest> {

    @Override
    public byte[] getSignatureMessage(RepropagateTransactionRequest repropagateTransactionRequest) {

        byte[] hashInBytes = repropagateTransactionRequest.getTransactionHash().toString().getBytes();
        byte[] createTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(repropagateTransactionRequest.getCreateTime().toEpochMilli()).array();
        byte[] restartTransactionRequestBuffer = ByteBuffer.allocate(hashInBytes.length + createTimeInBytes.length)
                .put(hashInBytes).put(createTimeInBytes).array();

        return CryptoHelper.cryptoHash(restartTransactionRequestBuffer).getBytes();
    }
}
