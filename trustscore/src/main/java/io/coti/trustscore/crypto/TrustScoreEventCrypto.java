package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.data.Events.KycEventData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class TrustScoreEventCrypto extends SignatureValidationCrypto<KycEventData> {
    @Override
    public byte[] getMessageInBytes(KycEventData eventData) {
        byte[] userHashInBytes = eventData.getUserHash().getBytes();

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + Long.BYTES + Integer.BYTES).
                put(userHashInBytes).putLong(eventData.getEventDate().getTime()).putInt(eventData.getEventType().getValue());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }
}
