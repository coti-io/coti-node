package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.http.GetMintingQuotesRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetMintingQuotesRequestCrypto extends SignatureValidationCrypto<GetMintingQuotesRequest> {

    @Override
    public byte[] getSignatureMessage(GetMintingQuotesRequest getMintingQuotesRequest) {
        byte[] userHashInBytes = getMintingQuotesRequest.getUserHash().getBytes();
        ByteBuffer getMintingQuotesRequestBuffer =
                ByteBuffer.allocate(userHashInBytes.length + Long.BYTES)
                        .put(userHashInBytes).putLong(getMintingQuotesRequest.getCreationTime().toEpochMilli());
        return CryptoHelper.cryptoHash(getMintingQuotesRequestBuffer.array()).getBytes();
    }
}
