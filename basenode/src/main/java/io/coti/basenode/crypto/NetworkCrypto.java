package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.TreeSet;

@Component
public class NetworkCrypto extends SignatureCrypto<NetworkData> {

    @Autowired
    private NetworkNodeCrypto networkNodeCrypto;

    @Override
    public byte[] getSignatureMessage(NetworkData networkData) {

        TreeSet<Hash> networkNodeHashSet = new TreeSet<>();

        networkData.getMultipleNodeMaps().values().forEach(
                nodeMap -> nodeMap.values().stream().filter(Objects::nonNull).forEach(
                        networkNodeData -> {
                            byte[] bytes = networkNodeCrypto.getSignatureMessage(networkNodeData);
                            networkNodeHashSet.add(new Hash(bytes));
                        }));
        networkData.getSingleNodeNetworkDataMap().values().stream().filter(Objects::nonNull).forEach(
                networkNodeData -> {
                    byte[] bytes = networkNodeCrypto.getSignatureMessage(networkNodeData);
                    networkNodeHashSet.add(new Hash(bytes));
                });


        if (networkNodeHashSet.isEmpty()) {
            return new byte[0];
        }

        int networkDataBufferLength = networkNodeHashSet.first().getBytes().length * networkNodeHashSet.size();
        ByteBuffer networkDataBuffer = ByteBuffer.allocate(networkDataBufferLength);
        networkNodeHashSet.forEach(hash -> networkDataBuffer.put(hash.getBytes()));
        return CryptoHelper.cryptoHash(networkDataBuffer.array()).getBytes();
    }

}
