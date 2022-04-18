package io.coti.basenode.crypto;

import io.coti.basenode.http.GetTokenHistoryRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetTokenHistoryRequestCrypto extends SignatureValidationCrypto<GetTokenHistoryRequest> {

    @Override
    public byte[] getSignatureMessage(GetTokenHistoryRequest getTokenHistoryRequest) {

        byte[] currencyHashInBytes = getTokenHistoryRequest.getCurrencyHash().getBytes();

        ByteBuffer getTokenHistoryRequestBuffer = ByteBuffer.allocate(currencyHashInBytes.length + Long.BYTES)
                .put(currencyHashInBytes).putLong(getTokenHistoryRequest.getCreateTime().toEpochMilli());
        return CryptoHelper.cryptoHash(getTokenHistoryRequestBuffer.array()).getBytes();
    }
}
