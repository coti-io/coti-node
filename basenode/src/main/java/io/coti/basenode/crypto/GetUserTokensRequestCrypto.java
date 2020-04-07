package io.coti.basenode.crypto;

import io.coti.basenode.http.GetUserTokensRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetUserTokensRequestCrypto extends SignatureValidationCrypto<GetUserTokensRequest> {

    @Override
    public byte[] getSignatureMessage(GetUserTokensRequest getUserTokensRequest) {

        final byte[] getUserTokensRequestTextInBytes = "GetUserTokensRequest".getBytes();
        byte[] userHashInBytes = getUserTokensRequest.getUserHash().getBytes();

        ByteBuffer getMintingHistoryRequestBuffer = ByteBuffer.allocate(getUserTokensRequestTextInBytes.length + userHashInBytes.length + Long.BYTES)
                .put(getUserTokensRequestTextInBytes).put(userHashInBytes).putLong(getUserTokensRequest.getCreationTime().toEpochMilli());
        return CryptoHelper.cryptoHash(getMintingHistoryRequestBuffer.array()).getBytes();
    }
}
