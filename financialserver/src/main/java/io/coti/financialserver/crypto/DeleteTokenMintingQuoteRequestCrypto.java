package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.http.DeleteTokenMintingQuoteRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class DeleteTokenMintingQuoteRequestCrypto extends SignatureValidationCrypto<DeleteTokenMintingQuoteRequest> {

    @Override
    public byte[] getSignatureMessage(DeleteTokenMintingQuoteRequest deleteTokenMintingQuoteRequest) {

        byte[] userHashInBytes = deleteTokenMintingQuoteRequest.getUserHash().getBytes();
        byte[] warrantFeeHashInBytes = deleteTokenMintingQuoteRequest.getWarrantFeeHash().getBytes();
        byte[] currencyHashInBytes = deleteTokenMintingQuoteRequest.getCurrencyHash().getBytes();
        ByteBuffer deleteTokenMintingQuoteRequestBuffer =
                ByteBuffer.allocate(userHashInBytes.length + warrantFeeHashInBytes.length + currencyHashInBytes.length + Long.BYTES)
                        .put(userHashInBytes).put(warrantFeeHashInBytes).put(currencyHashInBytes).putLong(deleteTokenMintingQuoteRequest.getRequestTime().toEpochMilli());
        return CryptoHelper.cryptoHash(deleteTokenMintingQuoteRequestBuffer.array()).getBytes();
    }
}
