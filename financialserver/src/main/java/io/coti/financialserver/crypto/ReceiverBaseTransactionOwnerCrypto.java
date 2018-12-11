package io.coti.financialserver.crypto;

import java.nio.ByteBuffer;
import org.springframework.stereotype.Service;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.ReceiverBaseTransactionOwnerData;

@Service
public class ReceiverBaseTransactionOwnerCrypto extends SignatureCrypto<ReceiverBaseTransactionOwnerData> {

    @Override
    public byte[] getMessageInBytes(ReceiverBaseTransactionOwnerData receiverBaseTransactionOwnerData) {

        int byteBufferLength;
        byte[] userHashInBytes = receiverBaseTransactionOwnerData.getReceiverBaseTransactionHash().getBytes();
        byte[] merchantHashInBytes = receiverBaseTransactionOwnerData.getMerchantHash().getBytes();

        byteBufferLength = userHashInBytes.length + merchantHashInBytes.length;

        ByteBuffer disputeDataBuffer = ByteBuffer.allocate(byteBufferLength);

        disputeDataBuffer.put(userHashInBytes);
        disputeDataBuffer.put(merchantHashInBytes);

        byte[] disputeDataInBytes = disputeDataBuffer.array();
        return CryptoHelper.cryptoHash(disputeDataInBytes).getBytes();
    }
}
