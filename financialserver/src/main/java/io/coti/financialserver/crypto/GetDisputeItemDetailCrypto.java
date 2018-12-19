package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.data.GetDisputeItemDetailData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetDisputeItemDetailCrypto extends SignatureCrypto<GetDisputeItemDetailData> {

    @Override
    public byte[] getMessageInBytes(GetDisputeItemDetailData getDisputeItemDetailData) {
        byte[] disputeHashInBytes = getDisputeItemDetailData.getDisputeHash().getBytes();
        byte[] itemIdInBytes = getDisputeItemDetailData.getItemId().toString().getBytes();

        int byteBufferLength = disputeHashInBytes.length + itemIdInBytes.length;

        ByteBuffer commentDataBuffer = ByteBuffer.allocate(byteBufferLength)
                .put(disputeHashInBytes).put(itemIdInBytes);

        byte[] commentDataInBytes = commentDataBuffer.array();
        return CryptoHelper.cryptoHash(commentDataInBytes).getBytes();
    }
}
