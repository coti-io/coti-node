package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeItemVoteData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class DisputeItemVoteCrypto extends SignatureCrypto<DisputeItemVoteData> {

    @Override
    public byte[] getSignatureMessage(DisputeItemVoteData disputeItemVoteData) {

        byte[] disputeHashInBytes = disputeItemVoteData.getDisputeHash().getBytes();
        byte[] statusInBytes = disputeItemVoteData.getStatus().toString().getBytes();

        int byteBufferLength = disputeHashInBytes.length + Long.BYTES + statusInBytes.length;

        ByteBuffer disputeItemVoteDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(disputeHashInBytes)
                .putLong(disputeItemVoteData.getItemId())
                .put(statusInBytes);

        byte[] disputeItemVoteDataInBytes = disputeItemVoteDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeItemVoteDataInBytes).getBytes();
    }
}

