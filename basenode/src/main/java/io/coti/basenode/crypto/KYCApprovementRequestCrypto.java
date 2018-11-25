package io.coti.basenode.crypto;

import io.coti.basenode.http.data.KYCApprovmentRequest;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class KYCApprovementRequestCrypto extends SignatureCrypto<KYCApprovmentRequest> {

    @Override
    public byte[] getMessageInBytes(KYCApprovmentRequest kycApprovmentRequest) {
        byte[] ccaApprovemnetInBytes = kycApprovmentRequest.getSignerHash().getBytes();
        ByteBuffer ccaApprovementRequestBuffer = ByteBuffer.allocate(ccaApprovemnetInBytes.length);
        ccaApprovementRequestBuffer.put(ccaApprovemnetInBytes);

        ZoneId zoneId = ZoneOffset.UTC;
        long unixTime = kycApprovmentRequest.getCreationTime().atZone(zoneId).toEpochSecond();
        ByteBuffer creationTimeBuffer = ByteBuffer.allocate(64);
        creationTimeBuffer.putLong(unixTime);

        ByteBuffer finalCCAApprovmentBuffer = ByteBuffer.allocate(64 + ccaApprovemnetInBytes.length)
                .put(ccaApprovementRequestBuffer.array()).put(creationTimeBuffer.array());

        byte[] finalCCAApprovmentBufferInBytes = finalCCAApprovmentBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(finalCCAApprovmentBufferInBytes).getBytes();

        return cryptoHashedMessage;
    }


}
