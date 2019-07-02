package io.coti.historynode.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.historynode.http.GetTransactionsByAddressRequest;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class TransactionsRequestCrypto extends SignatureValidationCrypto<GetTransactionsByAddressRequest> {

    @Override
    public byte[] getSignatureMessage(GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        byte[] addressInBytes = getTransactionsByAddressRequest.getAddress().getBytes();
        ByteBuffer transactionsRequestBuffer = ByteBuffer.allocate(addressInBytes.length+Long.BYTES+Long.BYTES).
                put(addressInBytes).putLong(getTransactionsByAddressRequest.getStartDate().toEpochMilli()).
                putLong(getTransactionsByAddressRequest.getEndDate().toEpochMilli());
        byte[] transactionsRequestInBytes = transactionsRequestBuffer.array();
        return CryptoHelper.cryptoHash(transactionsRequestInBytes).getBytes();
    }
}
