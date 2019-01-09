package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.RecourseClaimData;
import io.coti.financialserver.data.ResolveRecourseClaim;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ResolveRecourseClaimCrypto extends SignatureCrypto<ResolveRecourseClaim> {

    @Override
    public byte[] getSignatureMessage(ResolveRecourseClaim resolveRecourseClaim) {

        int byteBufferLength;
        byte[] merchantHashInBytes;
        byte[] disputeHashInBytes;
        byte[] transactionHashInBytes;

        merchantHashInBytes = resolveRecourseClaim.getMerchantHash().getBytes();
        byteBufferLength = merchantHashInBytes.length;

        disputeHashInBytes = resolveRecourseClaim.getDisputeHash().getBytes();
        byteBufferLength += disputeHashInBytes.length;

        transactionHashInBytes = resolveRecourseClaim.getTransactionHash().getBytes();
        byteBufferLength += transactionHashInBytes.length;

        ByteBuffer recourseClaimDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(merchantHashInBytes)
                .put(disputeHashInBytes)
                .put(transactionHashInBytes);

        byte[] recourseClaimDataInBytes = recourseClaimDataBuffer.array();
        return CryptoHelper.cryptoHash(recourseClaimDataInBytes).getBytes();
    }
}

