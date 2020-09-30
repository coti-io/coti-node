package io.coti.basenode.crypto;

import io.coti.basenode.http.GetUserTokensRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetUserTokensRequestCrypto extends SignatureValidationCrypto<GetUserTokensRequest> {

    @Override
    public byte[] getSignatureMessage(GetUserTokensRequest getUserTokensRequest) {

        final byte[] getUserTokensRequestTextInBytes = "/currencies/token/user".getBytes();
        byte[] userHashInBytes = getUserTokensRequest.getUserHash().getBytes();

        ByteBuffer getUserTokensRequestBuffer = ByteBuffer.allocate(getUserTokensRequestTextInBytes.length + userHashInBytes.length + Long.BYTES)
                .put(getUserTokensRequestTextInBytes).put(userHashInBytes).putLong(getUserTokensRequest.getCreateTime().toEpochMilli());
        return CryptoHelper.cryptoHash(getUserTokensRequestBuffer.array()).getBytes();
    }
}
