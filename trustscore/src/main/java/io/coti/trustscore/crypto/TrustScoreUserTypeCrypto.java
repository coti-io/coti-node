package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.http.SetUserTypeRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class TrustScoreUserTypeCrypto extends SignatureValidationCrypto<SetUserTypeRequest> {

    @Override
    public byte[] getMessageInBytes(SetUserTypeRequest setUserTypeRequest) {
        byte[] userType = setUserTypeRequest.getUserHash().getBytes();
        byte[] userTypeBytes = setUserTypeRequest.getUserType().getBytes(StandardCharsets.UTF_8);
        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userType.length + userTypeBytes.length);
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageBuffer.array()).getBytes();
        return cryptoHashedMessage;
    }
}
