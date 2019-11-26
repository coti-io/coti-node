package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.http.GetUserTokensRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetUserTokensRequestCrypto extends SignatureValidationCrypto<GetUserTokensRequest> {

    @Override
    public byte[] getSignatureMessage(GetUserTokensRequest getUserTokensRequest) {

        byte[] userHashInBytes = getUserTokensRequest.getUserHash().getBytes();

        ByteBuffer getMintingHistoryRequestBuffer = ByteBuffer.allocate(userHashInBytes.length + Long.BYTES)
                .put(userHashInBytes).putLong(getUserTokensRequest.getCreationTime().toEpochMilli());
        return CryptoHelper.cryptoHash(getMintingHistoryRequestBuffer.array()).getBytes();
    }
}
