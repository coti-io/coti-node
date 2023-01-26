package io.coti.historynode.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.basenode.http.GetTransactionsByAddressRequest;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class GetTransactionsByAddressRequestCrypto extends SignatureValidationCrypto<GetTransactionsByAddressRequest> {

    @Override
    public byte[] getSignatureMessage(GetTransactionsByAddressRequest getTransactionsByAddressRequest) {
        byte[] addressInBytes = getTransactionsByAddressRequest.getAddress().getBytes();
        byte[] startDateInBytes = getTransactionsByAddressRequest.getStartDate() != null ? getTransactionsByAddressRequest.getStartDate().toString().getBytes() : new byte[0];
        byte[] endDateInBytes = getTransactionsByAddressRequest.getEndDate() != null ? getTransactionsByAddressRequest.getEndDate().toString().getBytes() : new byte[0];
        ByteBuffer transactionsRequestBuffer = ByteBuffer.allocate(addressInBytes.length + startDateInBytes.length + endDateInBytes.length).
                put(addressInBytes).put(startDateInBytes).
                put(endDateInBytes);
        byte[] transactionsRequestInBytes = transactionsRequestBuffer.array();
        return CryptoHelper.cryptoHash(transactionsRequestInBytes).getBytes();
    }
}
