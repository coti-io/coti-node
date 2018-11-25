package io.coti.nodemanager.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.basenode.http.data.KYCApprovementResponse;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
@Component
public class KYCApprovementResponseCrypto extends SignatureCrypto<KYCApprovementResponse> {
    @Override
    public byte[] getMessageInBytes(KYCApprovementResponse kycApprovementResponse) {
        byte[] userHashInBytes = kycApprovementResponse.getUserHash().getBytes();

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + Double.BYTES).
                put(userHashInBytes).putDouble(kycApprovementResponse.getTrustScore());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }
}
