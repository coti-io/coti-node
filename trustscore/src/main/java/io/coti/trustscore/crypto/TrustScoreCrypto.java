package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.data.TrustScoreData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class TrustScoreCrypto extends SignatureValidationCrypto<TrustScoreData> {

    @Override
    public byte[] getSignatureMessage(TrustScoreData trustScoreData) {
        byte[] userHashInBytes = trustScoreData.getUserHash().getBytes();


        byte[] userTypeBytes = trustScoreData.getUserType().toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + Double.BYTES + userTypeBytes.length).
                put(userHashInBytes).putDouble(trustScoreData.getKycTrustScore()).put(userTypeBytes);

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        return CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
    }
}
