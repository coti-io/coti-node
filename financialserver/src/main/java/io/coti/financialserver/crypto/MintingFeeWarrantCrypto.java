package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.MintingFeeWarrantData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class MintingFeeWarrantCrypto extends SignatureCrypto<MintingFeeWarrantData> {

    @Override
    public byte[] getSignatureMessage(MintingFeeWarrantData mintingFeeWarrantData) {
        byte[] hashInBytes = mintingFeeWarrantData.getHash().toString().getBytes();
        byte[] requestTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(mintingFeeWarrantData.getRequestTime().toEpochMilli()).array();
        byte[] currencyHashInBytes = mintingFeeWarrantData.getCurrencyHash().getBytes();
        byte[] amountInBytes = mintingFeeWarrantData.getAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] feeGivenInBytes = mintingFeeWarrantData.getFeeForMinting().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);

        ByteBuffer feeWarrantBuffer = ByteBuffer.allocate(hashInBytes.length + requestTimeInBytes.length + currencyHashInBytes.length + amountInBytes.length + feeGivenInBytes.length)
                .put(hashInBytes).put(requestTimeInBytes).put(currencyHashInBytes).put(amountInBytes).put(feeGivenInBytes);

        return CryptoHelper.cryptoHash(feeWarrantBuffer.array()).getBytes();
    }

}
