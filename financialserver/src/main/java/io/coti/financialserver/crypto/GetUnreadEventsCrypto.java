package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.data.GetUnreadEventsData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.Instant;

@Component
public class GetUnreadEventsCrypto extends SignatureCrypto<GetUnreadEventsData> {

    @Override
    public byte[] getSignatureMessage(GetUnreadEventsData getUnreadEventsData) {
        Instant creationTime = getUnreadEventsData.getCreationTime();
        ByteBuffer getUnreadEventsBuffer = ByteBuffer.allocate(Long.BYTES).putLong(creationTime.getEpochSecond() * 1000 + creationTime.getNano() / 1000000);
        return CryptoHelper.cryptoHash(getUnreadEventsBuffer.array()).getBytes();
    }
}
