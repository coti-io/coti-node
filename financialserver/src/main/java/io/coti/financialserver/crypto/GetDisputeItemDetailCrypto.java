package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.data.GetDisputeItemDetailData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class GetDisputeItemDetailCrypto extends SignatureCrypto<GetDisputeItemDetailData> {

    @Override
    public byte[] getSignatureMessage(GetDisputeItemDetailData getDisputeItemDetailData) {
        byte[] disputeHashInBytes = getDisputeItemDetailData.getDisputeHash().getBytes();

        ByteBuffer commentDataBuffer = ByteBuffer.allocate(disputeHashInBytes.length + Long.BYTES)
                .put(disputeHashInBytes).putLong(getDisputeItemDetailData.getItemId());

        byte[] commentDataInBytes = commentDataBuffer.array();
        return CryptoHelper.cryptoHash(commentDataInBytes).getBytes();
    }
}
