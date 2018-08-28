package io.coti.common.crypto;

import io.coti.common.data.TransactionTrustScoreData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class TransactionTrustScoreCrypto extends SignatureCrypto<TransactionTrustScoreData> {
    @Override
    public byte[] getMessageInBytes(TransactionTrustScoreData transactionTrustScoreData) {
        byte[] userHashInBytes = transactionTrustScoreData.getUserHash().getBytes();

        byte[] transactionHashInBytes = transactionTrustScoreData.getTransactionHash().getBytes();

        ByteBuffer trustScoreBuffer = ByteBuffer.allocate(8);
        trustScoreBuffer.putDouble(transactionTrustScoreData.getTrustScore());

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + transactionHashInBytes.length + 8).
                put(userHashInBytes).put(transactionHashInBytes).put(trustScoreBuffer.array());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }


}
