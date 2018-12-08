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
        byte[] disputeHashInBytes = disputeData.getDisputeHash().getBytes();

        Integer byteBufferLength = disputeHashInBytes.length;

        ByteBuffer disputeDataBuffer = ByteBuffer.allocate(disputeHashInBytes.length)
                .put(disputeHashInBytes);

        byte[] disputeDataInBytes = disputeDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeDataInBytes).getBytes();
    }
}
