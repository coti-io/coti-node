package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.http.InsertDocumentScoreRequest;
import io.coti.trustscore.http.InsertEventScoreRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class InsertEventScoreCrypto extends SignatureValidationCrypto<InsertEventScoreRequest> {

    @Override
    public byte[] getSignatureMessage(InsertEventScoreRequest insertEventScoreRequest) {

        byte[] userHashInBytes = insertEventScoreRequest.getUserHash().getBytes();
        byte[] eventTypeBytes = insertEventScoreRequest.getEventType().toString().getBytes(StandardCharsets.UTF_8);
        byte[] eventIdentifierBytes = insertEventScoreRequest.getEventIdentifier().getBytes();

        ByteBuffer messageBuffer = ByteBuffer.allocate(userHashInBytes.length + eventTypeBytes.length + eventIdentifierBytes.length).
                put(userHashInBytes).put(eventTypeBytes).put(eventIdentifierBytes);

        byte[] messageBufferInBytes = messageBuffer.array();
        return CryptoHelper.cryptoHash(messageBufferInBytes).getBytes();
    }

}
