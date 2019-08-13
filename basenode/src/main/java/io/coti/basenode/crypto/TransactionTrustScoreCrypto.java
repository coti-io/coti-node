package io.coti.basenode.crypto;

import io.coti.basenode.data.TransactionTrustScoreData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class TransactionTrustScoreCrypto extends SignatureCrypto<TransactionTrustScoreData> {

    @Override
    public byte[] getSignatureMessage(TransactionTrustScoreData transactionTrustScoreData) {
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
