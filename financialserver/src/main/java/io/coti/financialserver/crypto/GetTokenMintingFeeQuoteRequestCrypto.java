package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.basenode.http.GetTokenMintingFeeQuoteRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class GetTokenMintingFeeQuoteRequestCrypto extends SignatureValidationCrypto<GetTokenMintingFeeQuoteRequest> {

    @Override
    public byte[] getSignatureMessage(GetTokenMintingFeeQuoteRequest getTokenMintingFeeQuoteRequest) {
        byte[] currencyHashInBytes = getTokenMintingFeeQuoteRequest.getCurrencyHash().getBytes();
        byte[] amountInBytes = getTokenMintingFeeQuoteRequest.getMintingAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] createTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(getTokenMintingFeeQuoteRequest.getCreateTime().toEpochMilli()).array();

        ByteBuffer getMintingQuotesRequestBuffer =
                ByteBuffer.allocate(currencyHashInBytes.length + amountInBytes.length + createTimeInBytes.length)
                        .put(currencyHashInBytes).put(amountInBytes).put(createTimeInBytes);
        return CryptoHelper.cryptoHash(getMintingQuotesRequestBuffer.array()).getBytes();
    }
}
