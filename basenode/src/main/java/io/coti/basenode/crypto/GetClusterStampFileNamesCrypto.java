package io.coti.basenode.crypto;

import io.coti.basenode.http.GetClusterStampFileNamesResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetClusterStampFileNamesCrypto extends SignatureCrypto<GetClusterStampFileNamesResponse> {

    @Override
    public byte[] getSignatureMessage(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse) {
        byte[] majorInBytes = getClusterStampFileNamesResponse.getMajor().getHash().getBytes();
        byte[] currenciesInBytes = getClusterStampFileNamesResponse.getCurrencies().getHash().getBytes();
        byte[] clusterStampBucketNameInBytes = getClusterStampFileNamesResponse.getClusterStampBucketName().getBytes();
        byte[] getClusterStampFileNamesInBytes = ByteBuffer.allocate(majorInBytes.length + currenciesInBytes.length + clusterStampBucketNameInBytes.length)
                .put(majorInBytes).put(currenciesInBytes).put(clusterStampBucketNameInBytes).array();
        return CryptoHelper.cryptoHash(getClusterStampFileNamesInBytes).getBytes();
    }
}
