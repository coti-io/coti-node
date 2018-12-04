package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.GetDocumentData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetDocumentCrypto extends SignatureCrypto<GetDocumentData> {

    @Override
    public byte[] getMessageInBytes(GetDocumentData documentData) {
        byte[] userHashInBytes = documentData.getSignerHash().getBytes();

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(userHashInBytes.length + Double.BYTES).put(userHashInBytes);

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

