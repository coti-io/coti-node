package io.coti.basenode.crypto;

import io.coti.basenode.data.ClusterStampNameData;
import io.coti.basenode.http.GetClusterStampFileNamesResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.List;

import static io.coti.basenode.crypto.CryptoHelper.DEFAULT_HASH_BYTE_SIZE;

@Service
public class GetClusterStampFileNamesCrypto extends SignatureCrypto<GetClusterStampFileNamesResponse> {

    @Override
    public byte[] getSignatureMessage(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse) {
        byte[] majorInBytes = getClusterStampFileNamesResponse.getMajor().getHash().getBytes();
        List<ClusterStampNameData> tokenClusterStampNames = getClusterStampFileNamesResponse.getTokenClusterStampNames();
        byte[] tokensInBytes = new byte[0];
        if (!tokenClusterStampNames.isEmpty()) {
            ByteBuffer tokenClusterStampNamesBuffer = ByteBuffer.allocate(tokenClusterStampNames.size() * DEFAULT_HASH_BYTE_SIZE);
            tokenClusterStampNames.forEach(clusterStampNameData -> tokenClusterStampNamesBuffer.put(clusterStampNameData.getHash().getBytes()));
            tokensInBytes = tokenClusterStampNamesBuffer.array();
        }
        byte[] clusterStampBucketNameInBytes = getClusterStampFileNamesResponse.getClusterStampBucketName().getBytes();
        byte[] getClusterStampFileNamesInBytes = ByteBuffer.allocate(majorInBytes.length + tokensInBytes.length + clusterStampBucketNameInBytes.length)
                .put(majorInBytes).put(tokensInBytes).put(clusterStampBucketNameInBytes).array();
        return CryptoHelper.cryptoHash(getClusterStampFileNamesInBytes).getBytes();
    }
}
