package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.NewDocumentData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class NewDocumentCrypto extends SignatureCrypto<NewDocumentData> {

    @Override
    public byte[] getMessageInBytes(NewDocumentData documentData) {
        byte[] userHashInBytes = documentData.getSignerHash().getBytes();
        Integer documentDataBufferLength = userHashInBytes.length + Double.BYTES +
                                            documentData.getDisputeHash().getBytes().length +
                                            documentData.getName().getBytes().length +
                                            documentData.getDescription().getBytes().length;

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(documentDataBufferLength)
                                                .put(userHashInBytes)
                                                .put(documentData.getDisputeHash().getBytes())
                                                .putLong(documentData.getItemId())
                                                .put(documentData.getName().getBytes())
                                                .put(documentData.getDescription().getBytes());

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

