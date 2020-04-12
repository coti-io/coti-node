package io.coti.basenode.crypto;

import io.coti.basenode.http.GetTokenDetailsRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetTokenDetailsRequestCrypto extends SignatureValidationCrypto<GetTokenDetailsRequest> {

    @Override
    public byte[] getSignatureMessage(GetTokenDetailsRequest getTokenDetailsRequest) {

        byte[] userHashInBytes = getTokenDetailsRequest.getUserHash().getBytes();
        byte[] currencyHashInBytes = getTokenDetailsRequest.getCurrencyHash().getBytes();

        ByteBuffer getTokenDetailsRequestBuffer = ByteBuffer.allocate(userHashInBytes.length + currencyHashInBytes.length + Long.BYTES)
                .put(userHashInBytes).put(currencyHashInBytes).putLong(getTokenDetailsRequest.getCreationTime().toEpochMilli());
        return CryptoHelper.cryptoHash(getTokenDetailsRequestBuffer.array()).getBytes();
    }
}
