package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.GetNetworkVotersResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.List;

@Service
public class GetNetworkVotersCrypto extends SignatureCrypto<GetNetworkVotersResponse> {

    @Override
    public byte[] getSignatureMessage(GetNetworkVotersResponse getNetworkVotersResponse) {
        Instant createTime = getNetworkVotersResponse.getCreateTime();
        List<Hash> potentialVotersNetworkSummary = getNetworkVotersResponse.getPotentialVotersNetworkSummary();
        int byteBufferSize = Long.BYTES + potentialVotersNetworkSummary.stream().mapToInt(hash -> hash.getBytes().length).sum();
        ByteBuffer getNetworkVoteResponseBuffer = ByteBuffer.allocate(byteBufferSize);
        getNetworkVoteResponseBuffer.putLong(createTime.toEpochMilli());
        potentialVotersNetworkSummary.forEach(hash -> getNetworkVoteResponseBuffer.put(hash.getBytes()));
        return CryptoHelper.cryptoHash(getNetworkVoteResponseBuffer.array()).getBytes();
    }
}
