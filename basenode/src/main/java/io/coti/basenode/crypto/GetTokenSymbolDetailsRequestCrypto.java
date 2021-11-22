package io.coti.basenode.crypto;

import io.coti.basenode.http.GetTokenSymbolDetailsRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetTokenSymbolDetailsRequestCrypto extends SignatureValidationCrypto<GetTokenSymbolDetailsRequest> {

    @Override
    public byte[] getSignatureMessage(GetTokenSymbolDetailsRequest getTokenSymbolDetailsRequest) {

        byte[] userHashInBytes = getTokenSymbolDetailsRequest.getUserHash().getBytes();
        byte[] symbolInBytes = getTokenSymbolDetailsRequest.getSymbol().getBytes();

        ByteBuffer getTokenDetailsRequestBuffer = ByteBuffer.allocate(userHashInBytes.length + symbolInBytes.length)
                .put(userHashInBytes).put(symbolInBytes);
        return CryptoHelper.cryptoHash(getTokenDetailsRequestBuffer.array()).getBytes();
    }
}
