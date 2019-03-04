package io.coti.basenode.crypto;

import io.coti.basenode.data.NodeRegistrationData;
import io.coti.basenode.http.GetNodeRegistrationResponse;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Component
public class NodeRegistrationCrypto extends SignatureValidationCrypto<NodeRegistrationData> {

    @Override
    public byte[] getSignatureMessage(NodeRegistrationData nodeRegistrationData) {

        byte[] nodeHashInBytes = nodeRegistrationData.getNodeHash().getBytes();

        byte[] nodeTypeInBytes = nodeRegistrationData.getNode().getBytes();

        byte[] networkTypeInBytes = nodeRegistrationData.getNetwork().getBytes();

        Instant creationTime = nodeRegistrationData.getCreationTime();
        byte[] creationTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(creationTime.getEpochSecond() * 1000 + creationTime.getNano() / 1000000).array();

        ByteBuffer nodeRegistrationBuffer = ByteBuffer.allocate(nodeHashInBytes.length + nodeTypeInBytes.length + networkTypeInBytes.length + creationTimeInBytes.length)
                                                      .put(nodeHashInBytes).put(nodeTypeInBytes).put(networkTypeInBytes).put(creationTimeInBytes);

        return CryptoHelper.cryptoHash(nodeRegistrationBuffer.array()).getBytes();
    }
}
