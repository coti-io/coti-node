package io.coti.basenode.crypto;

import io.coti.basenode.http.GetNodeRegistrationRequest;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Component
public class GetNodeRegistrationRequestCrypto implements SignatureCreationCrypto<GetNodeRegistrationRequest> {

    @Override
    public byte[] getSignatureMessage(GetNodeRegistrationRequest getNodeRegistrationRequest) {
        byte[] nodeTypeInBytes = getNodeRegistrationRequest.getNodeType().getBytes(StandardCharsets.UTF_8);
        byte[] networkTypeInBytes = getNodeRegistrationRequest.getNetworkType().getBytes(StandardCharsets.UTF_8);

        ByteBuffer nodeRegistrationBuffer = ByteBuffer.allocate(nodeTypeInBytes.length + networkTypeInBytes.length).put(nodeTypeInBytes).put(networkTypeInBytes);
        return CryptoHelper.cryptoHash(nodeRegistrationBuffer.array()).getBytes();
    }

}


