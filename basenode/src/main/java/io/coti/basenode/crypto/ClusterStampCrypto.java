package io.coti.basenode.crypto;

import io.coti.basenode.data.ClusterStampData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ClusterStampCrypto extends SignatureCrypto<ClusterStampData> {

    @Override
    public byte[] getSignatureMessage(ClusterStampData clusterStampData) {

        int byteBufferLength = 0;

        byte[] balanceMapInBytes = clusterStampData.getBalanceMap().toString().getBytes();
        byteBufferLength += balanceMapInBytes.length;

        ByteBuffer clusterStampDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(balanceMapInBytes);

        byte[] clusterStampInBytes = clusterStampDataBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampInBytes).getBytes();
    }
}
