package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.NewDisputeRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class NewDisputeCrypto extends SignatureCrypto<NewDisputeRequest> {

    @Override
    public byte[] getMessageInBytes(NewDisputeRequest disputeData) {

        byte[] signerHashInBytes = disputeData.getSignerHash().getBytes();
        byte[] transactionHashInBytes = disputeData.getTransactionHash().getBytes();
        byte[] amountInBytes = disputeData.getAmount().unscaledValue().toByteArray();
        byte[] itemsInBytes = disputeData.getDisputeItems().toString().getBytes();

        Integer byteBufferLength = signerHashInBytes.length + transactionHashInBytes.length + amountInBytes.length + itemsInBytes.length;

        ByteBuffer documentDataBuffer = ByteBuffer.allocate(byteBufferLength + Double.BYTES)
                                                  .put(signerHashInBytes)
                                                  .put(transactionHashInBytes)
                                                  .put(amountInBytes);

        byte[] documentDataInBytes = documentDataBuffer.array();
        return CryptoHelper.cryptoHash(documentDataInBytes).getBytes();
    }
}
