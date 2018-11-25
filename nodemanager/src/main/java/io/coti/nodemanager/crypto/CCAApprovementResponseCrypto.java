package io.coti.nodemanager.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.basenode.http.data.CCAApprovementResponse;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
@Component
public class CCAApprovementResponseCrypto extends SignatureCrypto<CCAApprovementResponse> {
    @Override
    public byte[] getMessageInBytes(CCAApprovementResponse ccaApprovementResponse) {
        byte[] userHashInBytes = ccaApprovementResponse.getUserHash().getBytes();

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + Double.BYTES).
                put(userHashInBytes).putDouble(ccaApprovementResponse.getTrustScore());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }
}
