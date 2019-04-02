package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.RecourseClaimData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class RecourseClaimCrypto extends SignatureCrypto<RecourseClaimData> {

    @Override
    public byte[] getSignatureMessage(RecourseClaimData recourseClaimData) {

        int byteBufferLength;
        byte[] merchantHashInBytes;
        byte[] disputeHashesInBytes;
        byte[] transactionHashesInBytes;

        merchantHashInBytes = recourseClaimData.getMerchantHash().getBytes();
        byteBufferLength = merchantHashInBytes.length;

        int disputeHashesBufferLength = 0;
        for (Hash disputeHash : recourseClaimData.getDisputeHashes()) {
            disputeHashesBufferLength += disputeHash.getBytes().length;
        }
        ByteBuffer disputeHashesBuffer = ByteBuffer.allocate(disputeHashesBufferLength);
        for (Hash disputeHash : recourseClaimData.getDisputeHashes()) {
            disputeHashesBuffer.put(disputeHash.getBytes());
        }
        disputeHashesInBytes = disputeHashesBuffer.array();
        byteBufferLength += merchantHashInBytes.length;

        int transactionHashesBufferLength = 0;
        for (Hash disputeHash : recourseClaimData.getDisputeHashes()) {
            transactionHashesBufferLength += disputeHash.getBytes().length;
        }
        ByteBuffer transactionHashesBuffer = ByteBuffer.allocate(transactionHashesBufferLength);
        for (Hash transactionHash : recourseClaimData.getTransactionHashes()) {
            transactionHashesBuffer.put(transactionHash.getBytes());
        }
        transactionHashesInBytes = transactionHashesBuffer.array();
        byteBufferLength += transactionHashesInBytes.length;

        ByteBuffer recourseClaimDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(merchantHashInBytes)
                .put(disputeHashesInBytes)
                .put(transactionHashesInBytes);

        byte[] recourseClaimDataInBytes = recourseClaimDataBuffer.array();
        return CryptoHelper.cryptoHash(recourseClaimDataInBytes).getBytes();
    }
}

