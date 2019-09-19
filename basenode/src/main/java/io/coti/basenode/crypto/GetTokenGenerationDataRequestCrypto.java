package io.coti.basenode.crypto;

import io.coti.basenode.http.GetTokenGenerationDataRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetTokenGenerationDataRequestCrypto extends SignatureCrypto<GetTokenGenerationDataRequest> {
    @Override
    public byte[] getSignatureMessage(GetTokenGenerationDataRequest getTokenGenerationDataRequest) {
        byte[] senderHashInBytes = getTokenGenerationDataRequest.getSenderHash().getBytes();

        ByteBuffer getTokenGenerationDataBuffer = ByteBuffer.allocate(senderHashInBytes.length).put(senderHashInBytes);
        return CryptoHelper.cryptoHash(getTokenGenerationDataBuffer.array()).getBytes();
    }
}
