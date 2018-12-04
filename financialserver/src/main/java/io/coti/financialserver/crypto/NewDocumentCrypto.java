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

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(userHashInBytes.length + Double.BYTES)
                                                .put(userHashInBytes)
                                                .putInt(documentData.getDisputeId())
                                                .putInt(documentData.getDocumentId());

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

