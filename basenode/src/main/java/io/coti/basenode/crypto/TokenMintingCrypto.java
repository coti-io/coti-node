package io.coti.basenode.crypto;

import io.coti.basenode.data.TokenMintingData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class TokenMintingCrypto extends SignatureValidationCrypto<TokenMintingData> {

    @Override
    public byte[] getSignatureMessage(TokenMintingData tokenMintingData) {
        byte[] bytesOfCurrencyHash = tokenMintingData.getMintingCurrencyHash().getBytes();
        byte[] bytesOfAmount = tokenMintingData.getMintingAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] bytesOfFeeAmount = tokenMintingData.getFeeAmount() != null ? tokenMintingData.getFeeAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] bytesOfReceiverAddress = tokenMintingData.getReceiverAddress().getBytes();

        ByteBuffer tokenMintingDataBuffer = ByteBuffer.allocate(bytesOfCurrencyHash.length + bytesOfAmount.length + bytesOfFeeAmount.length
                        + bytesOfReceiverAddress.length + Long.BYTES)
                .put(bytesOfCurrencyHash).put(bytesOfAmount).put(bytesOfFeeAmount).put(bytesOfReceiverAddress)
                .putLong(tokenMintingData.getCreateTime().toEpochMilli());
        return CryptoHelper.cryptoHash(tokenMintingDataBuffer.array()).getBytes();
    }
}
