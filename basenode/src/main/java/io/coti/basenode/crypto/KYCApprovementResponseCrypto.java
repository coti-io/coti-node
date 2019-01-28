package io.coti.basenode.crypto;

import io.coti.basenode.http.data.KYCApprovementResponse;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class KYCApprovementResponseCrypto extends SignatureCrypto<KYCApprovementResponse> {
    @Override
    public byte[] getSignatureMessage(KYCApprovementResponse kycApprovementResponse) {
        byte[] userHashInBytes = kycApprovementResponse.getUserHash().getBytes();
        ByteBuffer userHashBuffer = ByteBuffer.allocate(userHashInBytes.length);
        userHashBuffer.put(userHashInBytes);

        byte[] registrationHashInBytes = kycApprovementResponse.getUserHash().getBytes();
        ByteBuffer registrationHashBuffer = ByteBuffer.allocate(registrationHashInBytes.length);
        registrationHashBuffer.put(registrationHashInBytes);

        ByteBuffer nodeTypeBuffer = ByteBuffer.allocate(4);
        nodeTypeBuffer.putInt((kycApprovementResponse.getNodeType().ordinal()));

        ZoneId zoneId = ZoneOffset.UTC;
        long unixTime = kycApprovementResponse.getCreationTime().atZone(zoneId).toEpochSecond();
        ByteBuffer creationTimeBuffer = ByteBuffer.allocate(64);
        creationTimeBuffer.putLong(unixTime);

        ByteBuffer finalCCAApprovmentResponseBuffer = ByteBuffer.allocate(64 + 4 + registrationHashInBytes.length + userHashInBytes.length)
                .put(userHashBuffer.array()).put(registrationHashBuffer.array()).put(nodeTypeBuffer.array()).put(creationTimeBuffer.array());



        byte[] trustScoreMessageInBytes = finalCCAApprovmentResponseBuffer.array();
        return CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
    }
}
