package io.coti.basenode.crypto;

import io.coti.basenode.http.GenerateTokenRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GenerateTokenRequestCrypto extends SignatureCrypto<GenerateTokenRequest> {

    @Override
    public byte[] getSignatureMessage(GenerateTokenRequest generateTokenRequest) {

        byte[] transactionHashInBytes = generateTokenRequest.getTransactionHash().getBytes();
        byte[] currencyHashInBytes = generateTokenRequest.getOriginatorCurrencyData().calculateHash().getBytes();

        byte[] generateTokenRequestBufferInBytes = ByteBuffer.allocate(transactionHashInBytes.length + currencyHashInBytes.length).
                put(transactionHashInBytes).put(currencyHashInBytes).array();

        return CryptoHelper.cryptoHash(generateTokenRequestBufferInBytes).getBytes();
    }
}
