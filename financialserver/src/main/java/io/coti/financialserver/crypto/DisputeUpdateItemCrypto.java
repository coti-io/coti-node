package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeUpdateItemData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class DisputeUpdateItemCrypto extends SignatureCrypto<DisputeUpdateItemData> {

    @Override
    public byte[] getSignatureMessage(DisputeUpdateItemData disputeUpdateItemData) {

        byte[] disputeHashInBytes = disputeUpdateItemData.getDisputeHash().getBytes();
        byte[] itemIdsInBytes = new byte[0];
        if (disputeUpdateItemData.getItemIds() != null) {
            ByteBuffer itemIdsBuffer = ByteBuffer.allocate(disputeUpdateItemData.getItemIds().size() * Long.BYTES);
            disputeUpdateItemData.getItemIds().forEach(itemId -> itemIdsBuffer.putLong(itemId));
            itemIdsInBytes = itemIdsBuffer.array();
        }
        byte[] statusInBytes = disputeUpdateItemData.getStatus().toString().getBytes();

        int byteBufferLength = disputeHashInBytes.length + itemIdsInBytes.length + statusInBytes.length;
        ByteBuffer disputeUpdateDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(disputeHashInBytes)
                .put(itemIdsInBytes)
                .put(statusInBytes);

        byte[] disputeUpdateDataInBytes = disputeUpdateDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeUpdateDataInBytes).getBytes();
    }
}

