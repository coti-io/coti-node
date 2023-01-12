package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.basenode.data.MintingFeeQuoteData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class MintingFeeQuoteCrypto extends SignatureCrypto<MintingFeeQuoteData> {

    @Override
    public byte[] getSignatureMessage(MintingFeeQuoteData mintingFeeQuoteData) {
        byte[] requestTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(mintingFeeQuoteData.getCreateTime().toEpochMilli()).array();
        byte[] currencyHashInBytes = mintingFeeQuoteData.getCurrencyHash().getBytes();
        byte[] amountInBytes = mintingFeeQuoteData.getMintingAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] feeGivenInBytes = mintingFeeQuoteData.getMintingFee().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);

        ByteBuffer feeWarrantBuffer = ByteBuffer.allocate(requestTimeInBytes.length + currencyHashInBytes.length + amountInBytes.length + feeGivenInBytes.length)
                .put(requestTimeInBytes).put(currencyHashInBytes).put(amountInBytes).put(feeGivenInBytes);

        return CryptoHelper.cryptoHash(feeWarrantBuffer.array()).getBytes();
    }

}
