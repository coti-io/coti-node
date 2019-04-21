package io.coti.basenode.crypto;

import io.coti.basenode.data.ClusterStampData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ClusterStampCrypto extends SignatureCrypto<ClusterStampData> {

    @Override
    public byte[] getSignatureMessage(ClusterStampData clusterStampData) {

//        byte[] balanceMapHashesInBytes = clusterStampData.getBalanceMapHashes().toString().getBytes();
//        byte[] balanceMapAmountsInBytes = clusterStampData.getBalanceMapAmounts().toString().getBytes();
//
//        ByteBuffer clusterStampDataBuffer =
//                ByteBuffer.allocate(balanceMapHashesInBytes.length+balanceMapAmountsInBytes.length)
//                .put(balanceMapHashesInBytes).put(balanceMapAmountsInBytes);

        byte[] rowsAsBytes = clusterStampData.getBaosRowsBytes().toByteArray();
        ByteBuffer clusterStampDataBuffer =
                ByteBuffer.allocate(rowsAsBytes.length)
                        .put(rowsAsBytes);

        byte[] clusterStampInBytes = clusterStampDataBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampInBytes).getBytes();
    }
}
