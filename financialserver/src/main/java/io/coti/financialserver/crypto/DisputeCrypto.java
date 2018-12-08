package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.http.NewDisputeRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DisputeCrypto extends SignatureCrypto<DisputeData> {

    @Override
    public byte[] getMessageInBytes(DisputeData disputeData) {

        byte[] transactionHashInBytes = disputeData.getTransactionHash().getBytes();

        List<DisputeItemData> disputeItems = disputeData.getDisputeItems();
        ByteBuffer disputeItemIdsBuffer = ByteBuffer.allocate(disputeItems.size()*Long.BYTES);
        for(DisputeItemData disputeItemData: disputeItems){
            disputeItemIdsBuffer.putLong(disputeItemData.getId());
        }

        byte[] disputeItemIdsInBytes = disputeItemIdsBuffer.array();

        Integer byteBufferLength = transactionHashInBytes.length + disputeItemIdsInBytes.length;

        ByteBuffer disputeDataBuffer = ByteBuffer.allocate(byteBufferLength)
                                                  .put(transactionHashInBytes)
                                                  .put(disputeItemIdsInBytes);

        byte[] disputeDataInBytes = disputeDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeDataInBytes).getBytes();
    }
}
