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
        byte[] recoveryServerAddressInBytes = networkNodeData.getRecoveryServerAddress() != null ? networkNodeData.getRecoveryServerAddress().getBytes() : new byte[0];

        ByteBuffer networkNodeBuffer = ByteBuffer.allocate(nodeTypeInBytes.length + addressInBytes.length + httpPortInBytes.length + propagationPortInBytes.length + receivingPortInBytes.length + recoveryServerAddressInBytes.length)
                .put(nodeTypeInBytes).put(addressInBytes).put(httpPortInBytes).put(propagationPortInBytes).put(receivingPortInBytes).put(recoveryServerAddressInBytes);

        return CryptoHelper.cryptoHash(networkNodeBuffer.array()).getBytes();
    }

}
