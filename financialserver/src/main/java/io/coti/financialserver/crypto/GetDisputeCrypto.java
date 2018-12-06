package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.GetDisputeRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetDisputeCrypto extends SignatureCrypto<GetDisputeRequest> {

    @Override
    public byte[] getMessageInBytes(GetDisputeRequest disputeData) {
        byte[] signerHashInBytes = disputeData.getSignerHash().getBytes();
        byte[] disputeHashInBytes = disputeData.getDisputeHash().getBytes();

        Integer byteBufferLength = signerHashInBytes.length + disputeHashInBytes.length;

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(byteBufferLength + Double.BYTES)
                .put(signerHashInBytes)
                .put(disputeHashInBytes);

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}
