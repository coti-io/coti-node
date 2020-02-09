package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeCommentData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class DisputeCommentCrypto extends SignatureCrypto<DisputeCommentData> {

    @Override
    public byte[] getSignatureMessage(DisputeCommentData commentData) {

        byte[] disputeHashInBytes = commentData.getDisputeHash().getBytes();
        byte[] itemIdsInBytes = new byte[0];
        if (commentData.getItemIds() != null) {
            ByteBuffer itemIdsBuffer = ByteBuffer.allocate(commentData.getItemIds().size() * Long.BYTES);
            commentData.getItemIds().forEach(itemIdsBuffer::putLong);
            itemIdsInBytes = itemIdsBuffer.array();
        }
        byte[] commentInBytes = commentData.getComment().getBytes();

        int byteBufferLength = disputeHashInBytes.length + itemIdsInBytes.length + commentInBytes.length;

        ByteBuffer commentDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(disputeHashInBytes).put(itemIdsInBytes).put(commentInBytes);

        byte[] commentDataInBytes = commentDataBuffer.array();
        return CryptoHelper.cryptoHash(commentDataInBytes).getBytes();
    }
}

