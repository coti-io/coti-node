package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;

@Service
public class DisputeCrypto extends SignatureCrypto<DisputeData> {

    @Override
    public byte[] getSignatureMessage(DisputeData disputeData) {

        byte[] transactionHashInBytes = disputeData.getTransactionHash().getBytes();
        byte[] disputeItemsInBytes = new byte[0];

        List<DisputeItemData> disputeItems = disputeData.getDisputeItems();
        if (disputeItems != null && disputeItems.size() > 0) {
            int disputeItemsByteSize = 0;
            for (DisputeItemData disputeItemData : disputeItems) {
                disputeItemsByteSize += Long.BYTES + disputeItemData.getReason().toString().getBytes().length;
            }
            ByteBuffer disputeItemsBuffer = ByteBuffer.allocate(disputeItemsByteSize);
            disputeItems.forEach(disputeItemData -> disputeItemsBuffer.putLong(disputeItemData.getId()).put(disputeItemData.getReason().toString().getBytes()));
            disputeItemsInBytes = disputeItemsBuffer.array();
        }

        ByteBuffer disputeDataBuffer = ByteBuffer.allocate(transactionHashInBytes.length + disputeItemsInBytes.length)
                .put(transactionHashInBytes).put(disputeItemsInBytes);

        byte[] disputeDataInBytes = disputeDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeDataInBytes).getBytes();
    }
}
