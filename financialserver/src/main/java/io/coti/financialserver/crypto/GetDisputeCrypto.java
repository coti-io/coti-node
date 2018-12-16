package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.GetDisputeData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetDisputeCrypto extends SignatureCrypto<GetDisputeData> {

    @Override
    public byte[] getMessageInBytes(GetDisputeData getDisputeData) {

        byte [] userHashInBytes = getDisputeData.getUserHash().getBytes();
        byte [] disputeHashesInBytes = getDisputeData.getDisputeHashes().toString().getBytes();
        byte [] disputeSideInBytes = getDisputeData.getDisputeSide().toString().getBytes();
        int byteBufferLength = userHashInBytes.length + disputeHashesInBytes.length + disputeSideInBytes.length;

        ByteBuffer disputeDataBuffer = ByteBuffer.allocate(byteBufferLength).put(userHashInBytes).put(disputeHashesInBytes).put(disputeSideInBytes);

        byte[] disputeDataInBytes = disputeDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeDataInBytes).getBytes();
    }
}
