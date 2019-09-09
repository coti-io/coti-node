package io.coti.basenode.crypto;

import io.coti.basenode.data.ExpandedTransactionTrustScoreData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class ExpandedTransactionTrustScoreCrypto extends SignatureCrypto<ExpandedTransactionTrustScoreData> {

    @Override
    public byte[] getSignatureMessage(ExpandedTransactionTrustScoreData expandedTransactionTrustScoreData) {
        byte[] userHashInBytes = expandedTransactionTrustScoreData.getUserHash().getBytes();
        byte[] transactionHashInBytes = expandedTransactionTrustScoreData.getTransactionHash().getBytes();

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + transactionHashInBytes.length + Double.BYTES).
                put(userHashInBytes).put(transactionHashInBytes).putDouble(expandedTransactionTrustScoreData.getTrustScore());

        return CryptoHelper.cryptoHash(trustScoreMessageBuffer.array()).getBytes();
    }

}
