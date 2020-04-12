package io.coti.basenode.crypto;

import io.coti.basenode.http.GetUserTokensRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetUserTokensRequestCrypto extends SignatureValidationCrypto<GetUserTokensRequest> {

    @Override
    public byte[] getSignatureMessage(GetUserTokensRequest getUserTokensRequest) {

        byte[] userHashInBytes = getUserTokensRequest.getUserHash().getBytes();

        ByteBuffer getUserTokensRequestBuffer = ByteBuffer.allocate(userHashInBytes.length + Long.BYTES)
                .put(userHashInBytes).putLong(getUserTokensRequest.getCreationTime().toEpochMilli());
        return CryptoHelper.cryptoHash(getUserTokensRequestBuffer.array()).getBytes();
    }
}
