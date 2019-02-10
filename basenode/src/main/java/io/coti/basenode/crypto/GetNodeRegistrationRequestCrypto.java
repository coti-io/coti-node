package io.coti.basenode.crypto;

import io.coti.basenode.http.GetNodeRegistrationRequest;
import org.springframework.stereotype.Component;

@Component
public class GetNodeRegistrationRequestCrypto extends SignatureCreationCrypto<GetNodeRegistrationRequest> {

    @Override
    public byte[] getSignatureMessage(GetNodeRegistrationRequest getNodeRegistrationRequest) {
        byte[] nodeTypeInBytes = getNodeRegistrationRequest.getNodeType().toString().getBytes();

        return CryptoHelper.cryptoHash(nodeTypeInBytes).getBytes();
    }


}


