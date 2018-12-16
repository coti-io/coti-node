package io.coti.financialserver.crypto;

import java.nio.ByteBuffer;
import java.util.List;
import org.springframework.stereotype.Service;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemData;

@Service
public class DisputeCrypto extends SignatureCrypto<DisputeData> {

    @Override
    public byte[] getMessageInBytes(DisputeData disputeData) {

        int byteBufferLength;
        byte[] consumerHashInBytes;
        byte[] merchantHashInBytes = null;
        byte[] transactionHashInBytes = null;
        byte[] disputeItemIdsInBytes = null;

        consumerHashInBytes = disputeData.getConsumerHash().getBytes();
        byteBufferLength = consumerHashInBytes.length;

        if(disputeData.getMerchantHash() != null) {
            merchantHashInBytes = disputeData.getMerchantHash().getBytes();
            byteBufferLength += merchantHashInBytes.length;
        }

        if(disputeData.getTransactionHash() != null) {
            transactionHashInBytes = disputeData.getTransactionHash().getBytes();
            byteBufferLength += transactionHashInBytes.length;
        }

        List<DisputeItemData> disputeItems = disputeData.getDisputeItems();
        if(disputeItems != null && disputeItems.size() > 0) {
            ByteBuffer disputeItemIdsBuffer = ByteBuffer.allocate(disputeItems.size()*Long.BYTES);
            for(DisputeItemData disputeItemData: disputeItems){
                disputeItemIdsBuffer.putLong(disputeItemData.getId());
            }

            disputeItemIdsInBytes = disputeItemIdsBuffer.array();
            byteBufferLength += disputeItemIdsInBytes.length;
        }

        ByteBuffer disputeDataBuffer = ByteBuffer.allocate(byteBufferLength);

        disputeDataBuffer.put(consumerHashInBytes);

        if(merchantHashInBytes != null) {
            disputeDataBuffer.put(merchantHashInBytes);
        }

        if(transactionHashInBytes != null) {
            disputeDataBuffer.put(transactionHashInBytes);
        }

        if(disputeItemIdsInBytes != null) {
            disputeDataBuffer.put(disputeItemIdsInBytes);
        }

        byte[] disputeDataInBytes = disputeDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeDataInBytes).getBytes();
    }
}
