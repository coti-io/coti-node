package io.coti.basenode.crypto;

import io.coti.basenode.http.GetUserTokensRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetUserTokensRequestCrypto extends SignatureValidationCrypto<GetUserTokensRequest> {

    @Override
    public byte[] getSignatureMessage(GetUserTokensRequest getUserTokensRequest) {

        byte[] userHashInBytes = getUserTokensRequest.getUserHash().getBytes();
        ByteBuffer getUserTokensRequestBuffer = ByteBuffer.allocate(userHashInBytes.length).put(userHashInBytes);
        return CryptoHelper.cryptoHash(getUserTokensRequestBuffer.array()).getBytes();
    }
}
