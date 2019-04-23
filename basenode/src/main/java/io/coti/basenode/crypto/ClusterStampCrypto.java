package io.coti.basenode.crypto;

import io.coti.basenode.data.ClusterStampData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ClusterStampCrypto extends SignatureCrypto<ClusterStampData> {

    @Override
    public byte[] getSignatureMessage(ClusterStampData clusterStampData) {

        ByteBuffer clusterStampDataBuffer =
                ByteBuffer.allocate(clusterStampData.getMessageByteSize());
        clusterStampData.getSignatureMessage().forEach(byteArray -> clusterStampDataBuffer.put(byteArray));

        byte[] clusterStampInBytes = clusterStampDataBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampInBytes).getBytes();
    }
}
