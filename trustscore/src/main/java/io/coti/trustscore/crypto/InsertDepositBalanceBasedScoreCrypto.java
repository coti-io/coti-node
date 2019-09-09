package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.http.InsertDebtBalanceBasedScoreRequest;
import io.coti.trustscore.http.InsertDepositBalanceBasedScoreRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class InsertDepositBalanceBasedScoreCrypto extends SignatureValidationCrypto<InsertDepositBalanceBasedScoreRequest> {

    @Override
    public byte[] getSignatureMessage(InsertDepositBalanceBasedScoreRequest insertEventScoreRequest) {

        byte[] userHashInBytes = insertEventScoreRequest.getUserHash().getBytes();
        byte[] eventTypeBytes = insertEventScoreRequest.getEventType().toString().getBytes(StandardCharsets.UTF_8);
        byte[] amountInBytes = insertEventScoreRequest.getAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);
        byte[] eventIdentifierBytes = insertEventScoreRequest.getEventIdentifier().getBytes();

        ByteBuffer messageBuffer = ByteBuffer.allocate(userHashInBytes.length + eventTypeBytes.length + amountInBytes.length + eventIdentifierBytes.length).
                put(userHashInBytes).put(eventTypeBytes).put(amountInBytes).put(eventIdentifierBytes);

        byte[] messageBufferInBytes = messageBuffer.array();
        return CryptoHelper.cryptoHash(messageBufferInBytes).getBytes();
    }

}
