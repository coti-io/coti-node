package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeUpdateItemData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class DisputeUpdateItemCrypto extends SignatureCrypto<DisputeUpdateItemData> {

    @Override
    public byte[] getSignatureMessage(DisputeUpdateItemData disputeUpdateItemData) {

        byte[] disputeHashInBytes = disputeUpdateItemData.getDisputeHash().getBytes();
        ByteBuffer itemIdsBuffer = ByteBuffer.allocate(disputeUpdateItemData.getItemIds().size() * Long.BYTES);
        disputeUpdateItemData.getItemIds().forEach(itemIdsBuffer::putLong);
        byte[] itemIdsInBytes = itemIdsBuffer.array();
        byte[] statusInBytes = disputeUpdateItemData.getStatus().toString().getBytes(StandardCharsets.UTF_8);

        int byteBufferLength = disputeHashInBytes.length + itemIdsInBytes.length + statusInBytes.length;
        ByteBuffer disputeUpdateDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(disputeHashInBytes)
                .put(itemIdsInBytes)
                .put(statusInBytes);

        byte[] disputeUpdateDataInBytes = disputeUpdateDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeUpdateDataInBytes).getBytes();
    }
}

