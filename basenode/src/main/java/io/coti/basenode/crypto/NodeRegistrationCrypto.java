package io.coti.basenode.crypto;

import io.coti.basenode.data.NodeRegistrationData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.time.Instant;

@Component
public class NodeRegistrationCrypto extends SignatureValidationCrypto<NodeRegistrationData> {

    @Override
    public byte[] getSignatureMessage(NodeRegistrationData nodeRegistrationData) {

        byte[] nodeHashInBytes = nodeRegistrationData.getNodeHash().getBytes();

        byte[] nodeTypeInBytes = nodeRegistrationData.getNode().getBytes();

        byte[] networkTypeInBytes = nodeRegistrationData.getNetwork().getBytes();

        Instant creationTime = nodeRegistrationData.getCreationTime();
        byte[] creationTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(creationTime.toEpochMilli()).array();

        ByteBuffer nodeRegistrationBuffer = ByteBuffer.allocate(nodeHashInBytes.length + nodeTypeInBytes.length + networkTypeInBytes.length + creationTimeInBytes.length)
                .put(nodeHashInBytes).put(nodeTypeInBytes).put(networkTypeInBytes).put(creationTimeInBytes);

        return CryptoHelper.cryptoHash(nodeRegistrationBuffer.array()).getBytes();
    }
}
