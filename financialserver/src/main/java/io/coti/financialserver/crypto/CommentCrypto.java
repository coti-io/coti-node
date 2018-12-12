package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeCommentData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class CommentCrypto extends SignatureCrypto<DisputeCommentData> {

    @Override
    public byte[] getMessageInBytes(DisputeCommentData commentData) {

        int byteBufferLength;
        byte[] userHashInBytes;
        byte[] disputeHashInBytes;
        byte[] commentHashInBytes;
        byte[] commentInBytes = null;
        byte[] itemIdsInBytes = null;

        userHashInBytes = commentData.getUserHash().getBytes();
        byteBufferLength = userHashInBytes.length;
        disputeHashInBytes = commentData.getDisputeHash().getBytes();
        byteBufferLength += disputeHashInBytes.length;
        if(commentData.getItemIds() != null) {
            itemIdsInBytes = commentData.getItemIds().toString().getBytes();
            byteBufferLength += itemIdsInBytes.length;
        }

        if(commentData.getComment() != null) {
            commentInBytes = commentData.getComment().getBytes();
            byteBufferLength += commentInBytes.length;
        }

        if(commentData.getHash() != null) {
            commentHashInBytes = commentData.getHash().getBytes();
            byteBufferLength += commentHashInBytes.length;
        }

        ByteBuffer commentDataBuffer = ByteBuffer.allocate(byteBufferLength)
                                                    .put(userHashInBytes)
                                                    .put(disputeHashInBytes);

        if(itemIdsInBytes != null) {
            commentDataBuffer.put(itemIdsInBytes);
        }

        if(commentInBytes != null) {
            commentDataBuffer.put(commentInBytes);
        }

        byte[] documentDataInBytes = commentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

