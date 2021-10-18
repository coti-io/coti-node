package io.coti.basenode.crypto;

import io.coti.basenode.data.NetworkNodeData;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Component
public class NetworkNodeCrypto extends SignatureCrypto<NetworkNodeData> {

    @Override
    public byte[] getSignatureMessage(NetworkNodeData networkNodeData) {

        byte[] nodeTypeInBytes = networkNodeData.getNodeType().toString().getBytes();
        byte[] addressInBytes = networkNodeData.getAddress().getBytes();
        byte[] httpPortInBytes = networkNodeData.getHttpPort() != null ? networkNodeData.getHttpPort().getBytes() : new byte[0];
        byte[] propagationPortInBytes = networkNodeData.getPropagationPort() != null ? networkNodeData.getPropagationPort().getBytes() : new byte[0];
        byte[] receivingPortInBytes = networkNodeData.getReceivingPort() != null ? networkNodeData.getReceivingPort().getBytes() : new byte[0];
        byte[] networkTypeInBytes = networkNodeData.getNetworkType().toString().getBytes();
        byte[] webServerUrlInBytes = networkNodeData.getWebServerUrl() != null ? networkNodeData.getWebServerUrl().getBytes() : new byte[0];
        byte[] versionInBytes = networkNodeData.getVersion() != null ? networkNodeData.getVersion().getBytes() : new byte[0];

        int networkNodeBufferLength = nodeTypeInBytes.length + addressInBytes.length + httpPortInBytes.length + propagationPortInBytes.length +
                receivingPortInBytes.length + networkTypeInBytes.length + webServerUrlInBytes.length + versionInBytes.length;
        ByteBuffer networkNodeBuffer = ByteBuffer.allocate(networkNodeBufferLength)
                .put(nodeTypeInBytes).put(addressInBytes).put(httpPortInBytes)
                .put(propagationPortInBytes).put(receivingPortInBytes)
                .put(networkTypeInBytes).put(webServerUrlInBytes).put(versionInBytes);

        return CryptoHelper.cryptoHash(networkNodeBuffer.array()).getBytes();
    }

}
