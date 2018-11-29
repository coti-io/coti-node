package io.coti.basenode.crypto;

import io.coti.basenode.http.data.KYCApprovementRequest;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class KYCApprovementRequestCrypto extends SignatureCrypto<KYCApprovementRequest> {

    @Override
    public byte[] getMessageInBytes(KYCApprovementRequest kycApprovementRequest) {
        byte[] ccaApprovemnetInBytes = kycApprovementRequest.getSignerHash().getBytes();
        ByteBuffer ccaApprovementRequestBuffer = ByteBuffer.allocate(ccaApprovemnetInBytes.length);
        ccaApprovementRequestBuffer.put(ccaApprovemnetInBytes);

        ZoneId zoneId = ZoneOffset.UTC;
        long unixTime = kycApprovementRequest.getCreationTime().atZone(zoneId).toEpochSecond();
        ByteBuffer creationTimeBuffer = ByteBuffer.allocate(64);
        creationTimeBuffer.putLong(unixTime);

        ByteBuffer finalCCAApprovmentBuffer = ByteBuffer.allocate(64 + ccaApprovemnetInBytes.length)
                .put(ccaApprovementRequestBuffer.array()).put(creationTimeBuffer.array());

        byte[] finalCCAApprovmentBufferInBytes = finalCCAApprovmentBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(finalCCAApprovmentBufferInBytes).getBytes();

        return cryptoHashedMessage;
    }


}
