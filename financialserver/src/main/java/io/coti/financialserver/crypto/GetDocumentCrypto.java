package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.GetDocumentRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetDocumentCrypto extends SignatureCrypto<GetDocumentRequest> {

    @Override
    public byte[] getMessageInBytes(GetDocumentRequest documentData) {
        byte[] userHashInBytes = documentData.getSignerHash().getBytes();
        byte[] disputeHash = documentData.getDisputeHash().getBytes();
        Long itemId = documentData.getItemId();

        Integer documentDataBufferLength = userHashInBytes.length + Double.BYTES + disputeHash.length;

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(documentDataBufferLength)
                                                    .put(userHashInBytes)
                                                    .put(disputeHash)
                                                    .putLong(itemId);

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}

