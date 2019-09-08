package io.coti.trustscore.crypto;

import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.trustscore.http.GetTransactionTrustScoreRequest;
import org.springframework.stereotype.Component;

@Component
public class GetTransactionTrustScoreRequestCrypto extends SignatureValidationCrypto<GetTransactionTrustScoreRequest> {

    @Override
    public byte[] getSignatureMessage(GetTransactionTrustScoreRequest getTransactionTrustScoreRequest) {
        return getTransactionTrustScoreRequest.getTransactionHash().getBytes();
    }
}
