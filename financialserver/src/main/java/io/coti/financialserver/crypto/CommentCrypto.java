package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeCommentData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class CommentCrypto extends SignatureCrypto<DisputeCommentData> {

    @Override
    public byte[] getMessageInBytes(DisputeCommentData documentData) {

        int byteBufferLength;
        byte[] userHashInBytes;
        byte[] disputeHashInBytes;
        byte[] commentHashInBytes;
        byte[] commentInBytes = null;

        userHashInBytes = documentData.getUserHash().getBytes();
        byteBufferLength = userHashInBytes.length;
        disputeHashInBytes = documentData.getDisputeHash().getBytes();
        byteBufferLength += disputeHashInBytes.length;
        if(documentData.getItemId() != null) {
            byteBufferLength += Long.BYTES;
        }

        if(documentData.getComment() != null) {
            commentInBytes = documentData.getComment().getBytes();
            byteBufferLength += commentInBytes.length;
        }

        if(documentData.getHash() != null) {
            commentHashInBytes = documentData.getHash().getBytes();
            byteBufferLength += commentHashInBytes.length;
        }

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(byteBufferLength)
                                                    .put(userHashInBytes)
                                                    .put(disputeHashInBytes);

        if(documentData.getItemId() != null) {
            documentDataBuffer.putLong(documentData.getItemId());
        }

        if(commentInBytes != null) {
            documentDataBuffer.put(commentInBytes);
        }

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

