package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.http.GetUserTokensRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetUserTokensRequestCrypto extends SignatureValidationCrypto<GetUserTokensRequest> {

    @Override
    public byte[] getSignatureMessage(GetUserTokensRequest getUserTokensRequest) {
        ByteBuffer getUserTokensRequestBuffer = ByteBuffer.allocate(Long.BYTES).putLong(getUserTokensRequest.getCreationTime().toEpochMilli());
        return CryptoHelper.cryptoHash(getUserTokensRequestBuffer.array()).getBytes();
    }
}
