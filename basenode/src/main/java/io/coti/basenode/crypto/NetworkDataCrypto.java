package io.coti.basenode.crypto;

import io.coti.basenode.data.NetworkData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.TreeMap;

@Component
public class NetworkDataCrypto extends SignatureCrypto<NetworkData> {

    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;

    @Override
    public byte[] getSignatureMessage(NetworkData networkData) {

        TreeMap<String, byte[]> treeMap = new TreeMap<>();

        networkData.getMultipleNodeMaps().values().forEach(
                nodeMap -> nodeMap.values().stream().filter(Objects::nonNull).forEach(
                        networkNodeData -> {
                            byte[] hash = networkNodeCrypto.getSignatureMessage(networkNodeData);
                            treeMap.put(DatatypeConverter.printHexBinary(hash), hash);
                        }));
        networkData.getSingleNodeNetworkDataMap().values().stream().filter(Objects::nonNull).forEach(
                networkNodeData -> {
                    byte[] hash = networkNodeCrypto.getSignatureMessage(networkNodeData);
                    treeMap.put(DatatypeConverter.printHexBinary(hash), hash);
                });


        if (treeMap.isEmpty()) {
            return new byte[0];
        }

        int networkDataBufferLength = treeMap.firstEntry().getValue().length * treeMap.size();  // supposed all hashes have equal size
        ByteBuffer networkDataBuffer = ByteBuffer.allocate(networkDataBufferLength);
        treeMap.values().forEach(networkDataBuffer::put);
        return CryptoHelper.cryptoHash(networkDataBuffer.array()).getBytes();
    }

}
