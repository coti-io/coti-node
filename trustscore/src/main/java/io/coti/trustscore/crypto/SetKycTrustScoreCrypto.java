package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.data.TrustScoreData;
import io.coti.trustscore.http.SetKycTrustScoreRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class SetKycTrustScoreCrypto extends SignatureValidationCrypto<SetKycTrustScoreRequest> {

    @Override
    public byte[] getSignatureMessage(SetKycTrustScoreRequest setKycTrustScoreRequest) {

        byte[] userHashInBytes = setKycTrustScoreRequest.getUserHash().getBytes();
        byte[] userTypeBytes = setKycTrustScoreRequest.getUserType().getBytes(StandardCharsets.UTF_8);

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + Double.BYTES + userTypeBytes.length).
                put(userHashInBytes).putDouble(setKycTrustScoreRequest.getKycTrustScore()).put(userTypeBytes);

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        return CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
    }

}
