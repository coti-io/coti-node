package io.coti.basenode.crypto;

import io.coti.basenode.http.GetNodeRegistrationRequest;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class GetNodeRegistrationRequestCrypto extends SignatureCreationCrypto<GetNodeRegistrationRequest> {

    @Override
    public byte[] getSignatureMessage(GetNodeRegistrationRequest getNodeRegistrationRequest) {
        byte[] nodeTypeInBytes = getNodeRegistrationRequest.getNodeType().getBytes();
        byte[] networkTypeInBytes = getNodeRegistrationRequest.getNetworkType().getBytes();

        ByteBuffer nodeRegistrationBuffer = ByteBuffer.allocate(nodeTypeInBytes.length + networkTypeInBytes.length).put(nodeTypeInBytes).put(networkTypeInBytes);
        return CryptoHelper.cryptoHash(nodeRegistrationBuffer.array()).getBytes();
    }

}


