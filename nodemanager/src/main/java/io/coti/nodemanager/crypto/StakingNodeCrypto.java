package io.coti.nodemanager.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.nodemanager.http.SetNodeStakeRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

@Service
public class StakingNodeCrypto extends SignatureValidationCrypto<SetNodeStakeRequest> {

    @Override
    public byte[] getSignatureMessage(SetNodeStakeRequest setNodeStakeRequest) {
        byte[] nodeHashBytes = setNodeStakeRequest.getNodeHash().getBytes();
        String decimalStakeRepresentation = setNodeStakeRequest.getStake().stripTrailingZeros().toPlainString();
        byte[] stakeBytes = decimalStakeRepresentation.getBytes(StandardCharsets.UTF_8);

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(nodeHashBytes.length + stakeBytes.length);
        trustScoreMessageBuffer.put(nodeHashBytes).put(stakeBytes);
        return CryptoHelper.cryptoHash(trustScoreMessageBuffer.array()).getBytes();
    }
}
