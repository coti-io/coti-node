package io.coti.basenode.crypto;

import io.coti.basenode.data.ClusterStampData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ClusterStampCrypto extends SignatureCrypto<ClusterStampData> {

    @Override
    public byte[] getSignatureMessage(ClusterStampData clusterStampData) {

        int byteBufferLength = 0;

        Long lastDspConfirmed = clusterStampData.getLastDspConfirmed();
        byteBufferLength += Long.BYTES;

        byte[] balanceMapInBytes = clusterStampData.getBalanceMap().toString().getBytes();
        byteBufferLength += balanceMapInBytes.length;

        byte[] unconfirmedTransactionsInBytes = clusterStampData.getUnconfirmedTransactions().toString().getBytes();
        byteBufferLength += unconfirmedTransactionsInBytes.length;

        ByteBuffer clusterStampDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .putLong(lastDspConfirmed)
                .put(balanceMapInBytes)
                .put(unconfirmedTransactionsInBytes);

        byte[] clusterStampInBytes = clusterStampDataBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampInBytes).getBytes();
    }
}

