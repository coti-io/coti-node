package io.coti.financialserver.crypto;

import java.nio.ByteBuffer;
import org.springframework.stereotype.Service;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeDocumentData;

@Service
public class DocumentCrypto extends SignatureCrypto<DisputeDocumentData> {

    @Override
    public byte[] getMessageInBytes(DisputeDocumentData documentData) {

        int byteBufferLength;
        byte[] userHashInBytes;
        byte[] disputeHashInBytes;
        byte[] documentHashInBytes;
        byte[] nameInBytes = null;
        byte[] descriptionInBytes = null;
        byte[] itemIdsInBytes = null;

        userHashInBytes = documentData.getUserHash().getBytes();
        byteBufferLength = userHashInBytes.length;
        disputeHashInBytes = documentData.getDisputeHash().getBytes();
        byteBufferLength += disputeHashInBytes.length;
        if(documentData.getItemIds() != null) {
            itemIdsInBytes = documentData.getItemIds().toString().getBytes();
            byteBufferLength += itemIdsInBytes.length;
        }

        if(documentData.getName() != null) {
            nameInBytes = documentData.getName().getBytes();
            byteBufferLength += nameInBytes.length;
        }

        if(documentData.getDescription() != null) {
            descriptionInBytes = documentData.getDescription().getBytes();
            byteBufferLength += descriptionInBytes.length;
        }

        if(documentData.getHash() != null) {
            documentHashInBytes = documentData.getHash().getBytes();
            byteBufferLength += documentHashInBytes.length;
        }

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(byteBufferLength)
                                                    .put(userHashInBytes)
                                                    .put(disputeHashInBytes);

        if(itemIdsInBytes != null) {
            documentDataBuffer.put(itemIdsInBytes);
        }

        if(nameInBytes != null) {
            documentDataBuffer.put(nameInBytes);
        }

        if(descriptionInBytes != null) {
            documentDataBuffer.put(descriptionInBytes);
        }

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

