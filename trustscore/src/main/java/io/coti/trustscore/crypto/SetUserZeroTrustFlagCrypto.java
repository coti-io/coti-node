package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.http.SetKycTrustScoreRequest;
import io.coti.trustscore.http.SetUserZeroTrustFlagSignedRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class SetUserZeroTrustFlagCrypto extends SignatureValidationCrypto<SetUserZeroTrustFlagSignedRequest> {

    @Override
    public byte[] getSignatureMessage(SetUserZeroTrustFlagSignedRequest setUserZeroTrustFlagSignedRequest) {

        byte[] userHashInBytes = setUserZeroTrustFlagSignedRequest.getUserHash().getBytes();
        byte[] flagBytes;
        if (setUserZeroTrustFlagSignedRequest.isZeroTrustFlag()) {
            flagBytes = "1".getBytes(StandardCharsets.UTF_8);
        }
        else {
            flagBytes = "0".getBytes(StandardCharsets.UTF_8);
        }

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + flagBytes.length).
                put(userHashInBytes).put(flagBytes);

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        return CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
    }

}
