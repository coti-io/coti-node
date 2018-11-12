package io.coti.basenode.crypto;

import io.coti.basenode.data.NetworkNodeData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class NetworkNodeCrypto extends SignatureCrypto<NetworkNodeData> {
    @Override
    public byte[] getMessageInBytes(NetworkNodeData networkNodeData) {
        byte[] networkNodeInBytes = networkNodeData.getHash().getBytes();

        ByteBuffer networknodeinbytesByteBuffer = ByteBuffer.allocate(networkNodeInBytes.length);
        networknodeinbytesByteBuffer.put(networkNodeInBytes);

        ByteBuffer nodeTypeBuffer = ByteBuffer.allocate(4);
        nodeTypeBuffer.putInt(networkNodeData.getNodeType().ordinal());

        ByteBuffer httpFullAddressBuffer = ByteBuffer.allocate(networkNodeData.getHttpFullAddress().getBytes().length);
        httpFullAddressBuffer.put(networkNodeData.getHttpFullAddress().getBytes());

        ByteBuffer finalNetworkNodeBuffer = ByteBuffer.allocate(4 + networkNodeData.getHttpFullAddress().getBytes().length
        + networkNodeInBytes.length).put(nodeTypeBuffer.array())
                .put(networknodeinbytesByteBuffer.array()).put(httpFullAddressBuffer.array());

        byte[] networkNodeBufferInBytes = finalNetworkNodeBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(networkNodeBufferInBytes).getBytes();

        return cryptoHashedMessage;
    }

}
