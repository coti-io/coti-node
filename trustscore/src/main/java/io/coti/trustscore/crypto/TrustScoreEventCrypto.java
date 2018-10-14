package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.data.Events.CentralEventData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class TrustScoreEventCrypto extends SignatureValidationCrypto<CentralEventData> {
    @Override
    public byte[] getMessageInBytes(CentralEventData eventData) {
        byte[] userHashInBytes = eventData.getUserHash().getBytes();

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + Long.BYTES + Integer.BYTES).
                put(userHashInBytes).putLong(eventData.getEventDate().getTime()).putInt(eventData.getEventType().getValue());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }
}
