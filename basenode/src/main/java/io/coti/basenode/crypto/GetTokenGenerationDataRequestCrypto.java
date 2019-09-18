package io.coti.basenode.crypto;

import io.coti.basenode.http.GetTokenGenerationDataRequest;
import org.springframework.stereotype.Service;

@Service
public class GetTokenGenerationDataRequestCrypto extends SignatureCrypto<GetTokenGenerationDataRequest> {
    @Override
    public byte[] getSignatureMessage(GetTokenGenerationDataRequest signable) {
        //TODO 9/17/2019 astolia: implement
        return new byte[0];
    }
}
