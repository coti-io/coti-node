package io.coti.basenode.crypto;

import io.coti.basenode.http.GetTokenDetailsRequest;
import io.coti.basenode.http.GetTokenHistoryRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetTokenHistoryRequestCrypto extends SignatureValidationCrypto<GetTokenHistoryRequest> {

    @Override
    public byte[] getSignatureMessage(GetTokenHistoryRequest getTokenHistoryRequest) {

        byte[] userHashInBytes = getTokenHistoryRequest.getUserHash().getBytes();
        byte[] currencyHashInBytes = getTokenHistoryRequest.getCurrencyHash().getBytes();

        ByteBuffer getTokenHistoryRequestBuffer = ByteBuffer.allocate(userHashInBytes.length + currencyHashInBytes.length + Long.BYTES)
                .put(userHashInBytes).put(currencyHashInBytes).putLong(getTokenHistoryRequest.getCreateTime().toEpochMilli());
        return CryptoHelper.cryptoHash(getTokenHistoryRequestBuffer.array()).getBytes();
    }
}
