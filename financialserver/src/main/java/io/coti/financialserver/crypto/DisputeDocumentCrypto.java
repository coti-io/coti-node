package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeDocumentData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class DisputeDocumentCrypto extends SignatureCrypto<DisputeDocumentData> {

    @Override
    public byte[] getSignatureMessage(DisputeDocumentData documentData) {

        byte[] disputeHashInBytes = documentData.getDisputeHash().getBytes();
        byte[] itemIdsInBytes = new byte[0];
        if (documentData.getItemIds() != null) {
            ByteBuffer itemIdsBuffer = ByteBuffer.allocate(documentData.getItemIds().size() * Long.BYTES);
            documentData.getItemIds().forEach(itemIdsBuffer::putLong);
            itemIdsInBytes = itemIdsBuffer.array();
        }
        byte[] descriptionInBytes = documentData.getDescription() != null ? documentData.getDescription().getBytes(StandardCharsets.UTF_8) : new byte[0];

        int byteBufferLength = disputeHashInBytes.length + itemIdsInBytes.length + descriptionInBytes.length;
        ByteBuffer documentDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(disputeHashInBytes)
                .put(itemIdsInBytes)
                .put(descriptionInBytes);

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

