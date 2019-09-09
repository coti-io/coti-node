package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.http.InsertChargeBackFrequencyBasedScoreRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class InsertChargeBackFrequencyBasedScoreCrypto extends SignatureValidationCrypto<InsertChargeBackFrequencyBasedScoreRequest> {

    @Override
    public byte[] getSignatureMessage(InsertChargeBackFrequencyBasedScoreRequest insertChargeBackFrequencyBasedScoreRequest) {

        byte[] userHashInBytes = insertChargeBackFrequencyBasedScoreRequest.getUserHash().getBytes();
        byte[] eventIdentifierBytes = insertChargeBackFrequencyBasedScoreRequest.getEventIdentifier().getBytes();
        byte[] transactionHashBytes = insertChargeBackFrequencyBasedScoreRequest.getTransactionHash().getBytes();
        byte[] amountInBytes = insertChargeBackFrequencyBasedScoreRequest.getAmount().stripTrailingZeros().toPlainString().getBytes(StandardCharsets.UTF_8);

        ByteBuffer messageBuffer = ByteBuffer.allocate(userHashInBytes.length + eventIdentifierBytes.length + transactionHashBytes.length + amountInBytes.length).
                put(userHashInBytes).put(eventIdentifierBytes).put(transactionHashBytes).put(amountInBytes);

        byte[] messageBufferInBytes = messageBuffer.array();
        return CryptoHelper.cryptoHash(messageBufferInBytes).getBytes();
    }
}
