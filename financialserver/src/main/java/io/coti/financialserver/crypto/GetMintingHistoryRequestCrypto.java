package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.http.GetMintingHistoryRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetMintingHistoryRequestCrypto extends SignatureValidationCrypto<GetMintingHistoryRequest> {

    @Override
    public byte[] getSignatureMessage(GetMintingHistoryRequest getMintingHistoryRequest) {

        byte[] userHashInBytes = getMintingHistoryRequest.getUserHash().getBytes();
        byte[] createTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(getMintingHistoryRequest.getCreateTime().toEpochMilli()).array();

        ByteBuffer getMintingHistoryRequestBuffer = ByteBuffer.allocate(userHashInBytes.length + Long.BYTES)
                .put(userHashInBytes).put(createTimeInBytes);
        return CryptoHelper.cryptoHash(getMintingHistoryRequestBuffer.array()).getBytes();
    }
}
