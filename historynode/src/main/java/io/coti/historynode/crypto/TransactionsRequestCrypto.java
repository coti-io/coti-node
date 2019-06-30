package io.coti.historynode.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.historynode.http.GetTransactionsRequest;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class TransactionsRequestCrypto extends SignatureValidationCrypto<GetTransactionsRequest> {

    @Override
    public byte[] getSignatureMessage(GetTransactionsRequest getTransactionsRequest) {
        byte[] addressInBytes = getTransactionsRequest.getTransactionAddress().getBytes();
        ByteBuffer transactionsRequestBuffer = ByteBuffer.allocate(addressInBytes.length+Long.BYTES+Long.BYTES).
                put(addressInBytes).putLong(getTransactionsRequest.getStartDate().toEpochMilli()).
                putLong(getTransactionsRequest.getEndDate().toEpochMilli());
        byte[] transactionsRequestInBytes = transactionsRequestBuffer.array();
        return CryptoHelper.cryptoHash(transactionsRequestInBytes).getBytes();
    }
}
