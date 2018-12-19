package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeCommentData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class DisputeCommentCrypto extends SignatureCrypto<DisputeCommentData> {

    @Override
    public byte[] getMessageInBytes(DisputeCommentData commentData) {

        byte[] disputeHashInBytes = commentData.getDisputeHash().getBytes();
        byte[] itemIdsInBytes = commentData.getItemIds() != null ? commentData.getItemIds().toString().getBytes() : new byte[0];
        byte[] commentSideInBytes = commentData.getCommentSide().toString().getBytes();
        byte[] commentInBytes = commentData.getComment().getBytes();

        int byteBufferLength = disputeHashInBytes.length + itemIdsInBytes.length + commentSideInBytes.length + commentInBytes.length;

        ByteBuffer commentDataBuffer = ByteBuffer.allocate(byteBufferLength)
                                          .put(disputeHashInBytes).put(itemIdsInBytes).put(commentSideInBytes).put(commentInBytes);

        byte[] commentDataInBytes = commentDataBuffer.array();
        return CryptoHelper.cryptoHash(commentDataInBytes).getBytes();
    }
}

