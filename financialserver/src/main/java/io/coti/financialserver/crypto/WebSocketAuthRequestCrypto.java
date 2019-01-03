package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.WebSocketAuthRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class WebSocketAuthRequestCrypto extends SignatureCrypto<WebSocketAuthRequest> {

    @Override
    public byte[] getMessageInBytes(WebSocketAuthRequest webSocketAuthRequest) {

        byte[] userHashInBytes = webSocketAuthRequest.getUserHash().getBytes();
        int byteBufferLength = userHashInBytes.length;

        ByteBuffer wsRequestBuffer = ByteBuffer.allocate(byteBufferLength)
                                                    .put(userHashInBytes);

        byte[] wsRequestInBytes = wsRequestBuffer.array();
        return CryptoHelper.cryptoHash(wsRequestInBytes).getBytes();
    }
}

