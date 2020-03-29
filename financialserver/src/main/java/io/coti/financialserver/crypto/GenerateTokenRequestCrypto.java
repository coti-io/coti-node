package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.http.GenerateTokenRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GenerateTokenRequestCrypto extends SignatureValidationCrypto<GenerateTokenRequest> {
    //todo delete it
    @Override
    public byte[] getSignatureMessage(GenerateTokenRequest generateTokenRequest) {

        byte[] transactionHashInBytes = generateTokenRequest.getTransactionHash().getBytes();
        byte[] currencyHashInBytes = generateTokenRequest.getOriginatorCurrencyData().calculateHash().getBytes();

        byte[] generateTokenRequestBufferInBytes = ByteBuffer.allocate(transactionHashInBytes.length + currencyHashInBytes.length).
                put(transactionHashInBytes).put(currencyHashInBytes).array();

        return CryptoHelper.cryptoHash(generateTokenRequestBufferInBytes).getBytes();
    }
}
