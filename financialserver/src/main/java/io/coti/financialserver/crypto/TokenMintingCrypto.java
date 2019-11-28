package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.basenode.data.TokenMintingData;
import io.coti.financialserver.http.MintingTokenFeeRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class TokenMintingCrypto extends SignatureValidationCrypto<TokenMintingData> {

    @Override
    public byte[] getSignatureMessage(TokenMintingData tokenMintingData) {
        byte[] currencyHashInBytes = tokenMintingData.getMintingCurrencyHash().getBytes();
        byte[] amountBytes = tokenMintingData.getMintingAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] addressHashInBytes = tokenMintingData.getReceiverAddress().getBytes();

        byte[] mintingTokenFeeRequestBufferInBytes = ByteBuffer.allocate(currencyHashInBytes.length + amountBytes.length + addressHashInBytes.length + Long.BYTES).
                put(currencyHashInBytes).put(amountBytes).put(addressHashInBytes).putLong(tokenMintingData.getCreateTime().toEpochMilli()).array();

        return CryptoHelper.cryptoHash(mintingTokenFeeRequestBufferInBytes).getBytes();
    }
}
