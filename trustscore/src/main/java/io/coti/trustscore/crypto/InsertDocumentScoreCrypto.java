package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.http.InsertDocumentScoreRequest;
import io.coti.trustscore.http.SetKycTrustScoreRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class InsertDocumentScoreCrypto extends SignatureValidationCrypto<InsertDocumentScoreRequest> {

    @Override
    public byte[] getSignatureMessage(InsertDocumentScoreRequest insertDocumentScoreRequest) {

        byte[] userHashInBytes = insertDocumentScoreRequest.getUserHash().getBytes();
        byte[] documentTypeBytes = insertDocumentScoreRequest.getDocumentType().toString().getBytes(StandardCharsets.UTF_8);

        ByteBuffer messageBuffer = ByteBuffer.allocate(userHashInBytes.length + Double.BYTES + documentTypeBytes.length).
                put(userHashInBytes).putDouble(insertDocumentScoreRequest.getScore()).put(documentTypeBytes);

        byte[] messageBufferInBytes = messageBuffer.array();
        return CryptoHelper.cryptoHash(messageBufferInBytes).getBytes();
    }

}
