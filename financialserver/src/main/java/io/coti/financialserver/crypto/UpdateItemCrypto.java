package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeUpdateItemData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class UpdateItemCrypto extends SignatureCrypto<DisputeUpdateItemData> {

    @Override
    public byte[] getMessageInBytes(DisputeUpdateItemData itemData) {

        int byteBufferLength;
        byte[] consumerHashInBytes;
        byte[] disputeHashInBytes;
        byte[] itemIdsInBytes;
        byte[] reasonInBytes = null;
        byte[] statusInBytes = null;

        consumerHashInBytes = itemData.getUserHash().getBytes();
        byteBufferLength = consumerHashInBytes.length;
        disputeHashInBytes = itemData.getDisputeHash().getBytes();
        byteBufferLength += disputeHashInBytes.length;
        itemIdsInBytes = itemData.getIds().toString().getBytes();
        byteBufferLength += itemIdsInBytes.length;

        if(itemData.getReason() != null) {
            reasonInBytes = itemData.getReason().toString().getBytes();
            byteBufferLength += reasonInBytes.length;
        }

        if(itemData.getStatus() != null) {
            statusInBytes = itemData.getStatus().toString().getBytes();
            byteBufferLength += statusInBytes.length;
        }

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(byteBufferLength)
                                                    .put(consumerHashInBytes)
                                                    .put(disputeHashInBytes)
                                                    .put(itemIdsInBytes);

        if(reasonInBytes != null) {
            documentDataBuffer.put(reasonInBytes);
        }

        if(statusInBytes != null) {
            documentDataBuffer.put(statusInBytes);
        }

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

