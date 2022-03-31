package io.coti.basenode.crypto;

import io.coti.basenode.data.TokenMintingServiceData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class TokenMintingCrypto extends SignatureValidationCrypto<TokenMintingServiceData> {

    @Override
    public byte[] getSignatureMessage(TokenMintingServiceData tokenMintingServiceData) {
        byte[] bytesOfCurrencyHash = tokenMintingServiceData.getMintingCurrencyHash().getBytes();
        byte[] bytesOfAmount = tokenMintingServiceData.getMintingAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] bytesOfFeeAmount = tokenMintingServiceData.getFeeAmount() != null ? tokenMintingServiceData.getFeeAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] bytesOfReceiverAddress = tokenMintingServiceData.getReceiverAddress().getBytes();

        ByteBuffer tokenMintingDataBuffer = ByteBuffer.allocate(bytesOfCurrencyHash.length + bytesOfAmount.length + bytesOfFeeAmount.length
                        + bytesOfReceiverAddress.length + Long.BYTES)
                .put(bytesOfCurrencyHash).put(bytesOfAmount).put(bytesOfFeeAmount).put(bytesOfReceiverAddress)
                .putLong(tokenMintingServiceData.getCreateTime().toEpochMilli());
        return CryptoHelper.cryptoHash(tokenMintingDataBuffer.array()).getBytes();
    }
}
