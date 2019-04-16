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

        byte[] initialFundDataListInBytes = clusterStampData.getInitialFundDataList().toString().getBytes();
        byteBufferLength += initialFundDataListInBytes.length;

        ByteBuffer clusterStampDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(balanceMapInBytes)
                .put(initialFundDataListInBytes);

        byte[] clusterStampInBytes = clusterStampDataBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampInBytes).getBytes();
    }
}
