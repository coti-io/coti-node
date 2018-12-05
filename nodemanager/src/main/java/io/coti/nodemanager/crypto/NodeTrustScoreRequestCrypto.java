package io.coti.nodemanager.crypto;

import io.coti.basenode.crypto.BaseTransactionCryptoWrapper;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.data.NodeTrustScoreRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;

@Service
public class NodeTrustScoreRequestCrypto extends SignatureCrypto<NodeTrustScoreRequest> {

    @Override
    public byte[] getMessageInBytes(NodeTrustScoreRequest nodeTrustScoreRequest) {
        byte[] signerHashInBytes = nodeTrustScoreRequest.getSignerHash().getBytes();
        ByteBuffer signerHashBuffer = ByteBuffer.allocate(signerHashInBytes.length);
        signerHashBuffer.put(signerHashInBytes);

        byte[] nodesHashInBytes = getNodesHashesBytes(nodeTrustScoreRequest.getNodesHash());
        ByteBuffer nodeBytesBuffer = ByteBuffer.allocate(nodesHashInBytes.length);
        nodeBytesBuffer.put(nodesHashInBytes);

        ByteBuffer finalBuffer = ByteBuffer.allocate(signerHashInBytes.length + nodesHashInBytes.length);
        finalBuffer.put(signerHashBuffer.array()).put(nodeBytesBuffer.array());

        byte[] finalBufferInBytes = finalBuffer.array();
        return CryptoHelper.cryptoHash(finalBufferInBytes).getBytes();
    }

    private byte[] getNodesHashesBytes(List<Hash> nodesHash) {
        ByteBuffer nodesHashBuffer = ByteBuffer.allocate(nodesHash.size() * nodesHash.get(0).getBytes().length);
        for (Hash nodeHash : nodesHash) {
            byte[] baseTransactionHashBytes = nodeHash.getBytes();
            nodesHashBuffer = nodesHashBuffer.put(baseTransactionHashBytes);
        }
        return nodesHashBuffer.array();
    }
}
