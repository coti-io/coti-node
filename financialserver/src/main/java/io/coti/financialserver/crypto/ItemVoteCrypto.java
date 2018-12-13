package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeItemVoteData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ItemVoteCrypto extends SignatureCrypto<DisputeItemVoteData> {

    @Override
    public byte[] getMessageInBytes(DisputeItemVoteData itemVoteData) {

        int byteBufferLength;
        byte[] userHashInBytes;
        byte[] disputeHashInBytes;
        Long itemIdInBytes;
        byte[] statusInBytes = null;

        userHashInBytes = itemVoteData.getUserHash().getBytes();
        byteBufferLength = userHashInBytes.length;
        disputeHashInBytes = itemVoteData.getDisputeHash().getBytes();
        byteBufferLength += disputeHashInBytes.length;
        itemIdInBytes = itemVoteData.getItemId();
        byteBufferLength += Long.BYTES;
        statusInBytes = itemVoteData.getStatus().toString().getBytes();
        byteBufferLength += statusInBytes.length;

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(byteBufferLength)
                                                    .put(userHashInBytes)
                                                    .put(disputeHashInBytes)
                                                    .put(statusInBytes)
                                                    .putLong(itemIdInBytes);

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

