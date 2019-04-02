package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.http.DisputeEventReadRequest;
import org.springframework.stereotype.Component;

@Component
public class DisputeEventReadCrypto extends SignatureValidationCrypto<DisputeEventReadRequest> {

    @Override
    public byte[] getSignatureMessage(DisputeEventReadRequest disputeEventReadRequest) {
        return disputeEventReadRequest.getDisputeEventHash().getBytes();
    }
}
