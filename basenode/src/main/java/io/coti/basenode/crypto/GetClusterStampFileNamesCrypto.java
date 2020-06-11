package io.coti.basenode.crypto;

import io.coti.basenode.http.GetClusterStampFileNamesResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetClusterStampFileNamesCrypto extends SignatureCrypto<GetClusterStampFileNamesResponse> {

    @Override
    public byte[] getSignatureMessage(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse) {
        byte[] clusterStampNameInBytes = getClusterStampFileNamesResponse.getClusterStampName().getHash().getBytes();
        byte[] clusterStampBucketNameInBytes = getClusterStampFileNamesResponse.getClusterStampBucketName().getBytes();
        byte[] getClusterStampFileNamesInBytes = ByteBuffer.allocate(clusterStampNameInBytes.length + clusterStampBucketNameInBytes.length)
                .put(clusterStampNameInBytes).put(clusterStampBucketNameInBytes).array();
        return CryptoHelper.cryptoHash(getClusterStampFileNamesInBytes).getBytes();
    }
}
