package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.http.GetTokenMintingFeeQuoteRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class GetTokenMintingFeeQuoteRequestCrypto extends SignatureValidationCrypto<GetTokenMintingFeeQuoteRequest> {

    @Override
    public byte[] getSignatureMessage(GetTokenMintingFeeQuoteRequest getTokenMintingFeeQuoteRequest) {
        byte[] userHashInBytes = getTokenMintingFeeQuoteRequest.getUserHash().getBytes();
        byte[] currencyHashInBytes = getTokenMintingFeeQuoteRequest.getCurrencyHash().getBytes();
        byte[] amountInBytes = getTokenMintingFeeQuoteRequest.getAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] requestTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(getTokenMintingFeeQuoteRequest.getRequestTime().toEpochMilli()).array();

        ByteBuffer getMintingQuotesRequestBuffer =
                ByteBuffer.allocate(userHashInBytes.length + currencyHashInBytes.length + amountInBytes.length + requestTimeInBytes.length)
                        .put(userHashInBytes).put(currencyHashInBytes).put(amountInBytes).put(requestTimeInBytes);
        return CryptoHelper.cryptoHash(getMintingQuotesRequestBuffer.array()).getBytes();
    }
}
