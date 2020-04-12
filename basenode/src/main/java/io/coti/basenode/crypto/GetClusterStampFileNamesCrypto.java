package io.coti.basenode.crypto;

import io.coti.basenode.http.GetClusterStampFileNamesResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetClusterStampFileNamesCrypto extends SignatureCrypto<GetClusterStampFileNamesResponse> {

    @Override
    public byte[] getSignatureMessage(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse) {
        byte[] currencyClusterStampNameInBytes = getClusterStampFileNamesResponse.getCurrencyClusterStampName().getHash().getBytes();
        byte[] balanceClusterStampNameInBytes = getClusterStampFileNamesResponse.getBalanceClusterStampName().getHash().getBytes();
        byte[] clusterStampBucketNameInBytes = getClusterStampFileNamesResponse.getClusterStampBucketName().getBytes();
        byte[] getClusterStampFileNamesInBytes = ByteBuffer.allocate(balanceClusterStampNameInBytes.length + currencyClusterStampNameInBytes.length + clusterStampBucketNameInBytes.length)
                .put(balanceClusterStampNameInBytes).put(currencyClusterStampNameInBytes).put(clusterStampBucketNameInBytes).array();
        return CryptoHelper.cryptoHash(getClusterStampFileNamesInBytes).getBytes();
    }
}
