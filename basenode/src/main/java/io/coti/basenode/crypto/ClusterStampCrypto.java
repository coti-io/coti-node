package io.coti.basenode.crypto;

import io.coti.basenode.data.ClusterStampData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ClusterStampCrypto extends SignatureCrypto<ClusterStampData> {

    @Override
    public byte[] getSignatureMessage(ClusterStampData clusterStampData) {

        int byteBufferLength = 0;

        byte[] clusterStampHashInBytes = clusterStampData.getHash().getBytes();
        byteBufferLength += clusterStampHashInBytes.length;

        byte[] balanceMapInBytes = clusterStampData.getBalanceMap().toString().getBytes();
        byteBufferLength += balanceMapInBytes.length;

        byte[] unconfirmedTransactionHashesInBytes = clusterStampData.getUnconfirmedTransactions().keySet().toString().getBytes();
        byteBufferLength += unconfirmedTransactionHashesInBytes.length;

        ByteBuffer clusterStampDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(clusterStampHashInBytes)
                .put(balanceMapInBytes)
                .put(unconfirmedTransactionHashesInBytes);

        byte[] clusterStampInBytes = clusterStampDataBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampInBytes).getBytes();
    }
}

