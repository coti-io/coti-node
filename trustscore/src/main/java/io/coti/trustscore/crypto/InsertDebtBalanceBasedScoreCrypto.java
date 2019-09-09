package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.http.InsertDebtBalanceBasedScoreRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class InsertDebtBalanceBasedScoreCrypto extends SignatureValidationCrypto<InsertDebtBalanceBasedScoreRequest> {

    @Override
    public byte[] getSignatureMessage(InsertDebtBalanceBasedScoreRequest insertEventScoreRequest) {

        byte[] userHashInBytes = insertEventScoreRequest.getUserHash().getBytes();
        byte[] eventTypeBytes = insertEventScoreRequest.getEventType().toString().getBytes(StandardCharsets.UTF_8);
        byte[] amountInBytes = insertEventScoreRequest.getAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] eventIdentifierBytes = insertEventScoreRequest.getEventIdentifier().getBytes();
        byte[] otherUserHashInBytes = insertEventScoreRequest.getOtherUserHash().getBytes();

        ByteBuffer messageBuffer = ByteBuffer.allocate(userHashInBytes.length + eventTypeBytes.length + amountInBytes.length + eventIdentifierBytes.length + otherUserHashInBytes.length).
                put(userHashInBytes).put(eventTypeBytes).put(amountInBytes).put(eventIdentifierBytes).put(otherUserHashInBytes);

        byte[] messageBufferInBytes = messageBuffer.array();
        return CryptoHelper.cryptoHash(messageBufferInBytes).getBytes();
    }

}
