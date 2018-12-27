package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeItemData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ItemCrypto extends SignatureCrypto<DisputeItemData> {

    @Override
    public byte[] getMessageInBytes(DisputeItemData itemData) {

        int byteBufferLength;
        byte[] userHashInBytes;
        byte[] disputeHashInBytes;
        Long itemIdInBytes;
        byte[] reasonInBytes = null;
        byte[] statusInBytes = null;

        userHashInBytes = itemData.getUserHash().getBytes();
        byteBufferLength = userHashInBytes.length;
        disputeHashInBytes = itemData.getDisputeHash().getBytes();
        byteBufferLength += disputeHashInBytes.length;
        itemIdInBytes = itemData.getId();
        byteBufferLength += Long.BYTES;

        if (itemData.getReason() != null) {
            reasonInBytes = itemData.getReason().toString().getBytes();
            byteBufferLength += reasonInBytes.length;
        }

        if (itemData.getStatus() != null) {
            statusInBytes = itemData.getStatus().toString().getBytes();
            byteBufferLength += statusInBytes.length;
        }

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(userHashInBytes)
                .put(disputeHashInBytes)
                .putLong(itemIdInBytes);

        if (reasonInBytes != null) {
            documentDataBuffer.put(reasonInBytes);
        }

        if (statusInBytes != null) {
            documentDataBuffer.put(statusInBytes);
        }

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

