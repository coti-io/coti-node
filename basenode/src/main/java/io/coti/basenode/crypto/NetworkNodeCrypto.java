package io.coti.basenode.crypto;

import io.coti.basenode.data.NetworkNode;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class NetworkNodeCrypto extends SignatureCrypto<NetworkNode> {
    @Override
    public byte[] getMessageInBytes(NetworkNode networkNode) {
        byte[] networkNodeInBytes = networkNode.getHash().getBytes();

        ByteBuffer networknodeinbytesByteBuffer = ByteBuffer.allocate(networkNodeInBytes.length);
        networknodeinbytesByteBuffer.put(networkNodeInBytes);

        ByteBuffer nodeTypeBuffer = ByteBuffer.allocate(4);
        nodeTypeBuffer.putInt(networkNode.getNodeType().ordinal());

        ByteBuffer httpFullAddressBuffer = ByteBuffer.allocate(networkNode.getHttpFullAddress().getBytes().length);
        httpFullAddressBuffer.put(networkNode.getHttpFullAddress().getBytes());

        ByteBuffer finalNetworkNodeBuffer = ByteBuffer.allocate(4 + networkNode.getHttpFullAddress().getBytes().length
        + networkNodeInBytes.length).put(nodeTypeBuffer.array())
                .put(networknodeinbytesByteBuffer.array()).put(httpFullAddressBuffer.array());

        byte[] networkNodeBufferInBytes = finalNetworkNodeBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(networkNodeBufferInBytes).getBytes();

        return cryptoHashedMessage;
    }

}
