package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.data.ReceiverBaseTransactionOwnerData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class ReceiverBaseTransactionOwnerCrypto extends SignatureValidationCrypto<ReceiverBaseTransactionOwnerData> {

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
