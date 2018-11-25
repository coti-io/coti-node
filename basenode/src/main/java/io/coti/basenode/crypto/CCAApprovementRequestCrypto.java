package io.coti.basenode.crypto;

import io.coti.basenode.http.data.CCAApprovementResponse;
import io.coti.basenode.http.data.CCAApprovmentRequest;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class CCAApprovementRequestCrypto extends SignatureCrypto<CCAApprovmentRequest> {

    @Override
    public byte[] getMessageInBytes(CCAApprovmentRequest ccaApprovmentRequest) {
        byte[] ccaApprovemnetInBytes = ccaApprovmentRequest.getSignerHash().getBytes();
        ByteBuffer ccaApprovementRequestBuffer = ByteBuffer.allocate(ccaApprovemnetInBytes.length);
        ccaApprovementRequestBuffer.put(ccaApprovemnetInBytes);

        ZoneId zoneId = ZoneOffset.UTC;
        long unixTime = ccaApprovmentRequest.getCreationTime().atZone(zoneId).toEpochSecond();
        ByteBuffer creationTimeBuffer = ByteBuffer.allocate(64);
        creationTimeBuffer.putLong(unixTime);

        ByteBuffer finalCCAApprovmentBuffer = ByteBuffer.allocate(64 + ccaApprovemnetInBytes.length)
                .put(ccaApprovementRequestBuffer.array()).put(creationTimeBuffer.array());

        byte[] finalCCAApprovmentBufferInBytes = finalCCAApprovmentBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(finalCCAApprovmentBufferInBytes).getBytes();

        return cryptoHashedMessage;
    }


}
