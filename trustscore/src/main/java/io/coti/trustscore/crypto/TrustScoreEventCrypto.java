package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.data.events.KycEventData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class TrustScoreEventCrypto extends SignatureValidationCrypto<KycEventData> {
    @Override
    public byte[] getSignatureMessage(KycEventData eventData) {
        byte[] userHashInBytes = eventData.getUserHash().getBytes();

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + Long.BYTES + Integer.BYTES).
                put(userHashInBytes).putLong(eventData.getEventDate().toEpochMilli()).putInt(eventData.getEventType().getValue());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        return CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
    }
}
