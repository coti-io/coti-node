package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.data.TrustScoreData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class TrustScoreCrypto extends SignatureValidationCrypto<TrustScoreData> {

    @Override
    public byte[] getSignatureMessage(TrustScoreData trustScoreData) {
        byte[] userHashInBytes = trustScoreData.getUserHash().getBytes();

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + Double.BYTES).
                put(userHashInBytes).putDouble(trustScoreData.getKycTrustScore());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }
}
