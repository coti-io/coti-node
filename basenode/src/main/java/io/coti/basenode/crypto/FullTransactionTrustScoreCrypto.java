package io.coti.basenode.crypto;

import io.coti.basenode.data.FullTransactionTrustScoreData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class FullTransactionTrustScoreCrypto extends SignatureCrypto<FullTransactionTrustScoreData> {

    @Override
    public byte[] getSignatureMessage(FullTransactionTrustScoreData fullTransactionTrustScoreData) {
        byte[] userHashInBytes = fullTransactionTrustScoreData.getUserHash().getBytes();

        byte[] transactionHashInBytes = fullTransactionTrustScoreData.getTransactionHash().getBytes();

        ByteBuffer trustScoreBuffer = ByteBuffer.allocate(8);
        trustScoreBuffer.putDouble(fullTransactionTrustScoreData.getTrustScore());

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + transactionHashInBytes.length + 8).
                put(userHashInBytes).put(transactionHashInBytes).put(trustScoreBuffer.array());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }

}
