package io.coti.basenode.crypto;

import io.coti.basenode.data.ClusterStampNameData;
import io.coti.basenode.http.GetClusterStampFileNamesResponse;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class GetClusterStampFileNamesCrypto extends SignatureCrypto<GetClusterStampFileNamesResponse>{

    //TODO 8/28/2019 astolia: NOTE - i am using  ClusterStampNameData 'hash' field to make the signature.
    // However, hash is not immutable and can be changed manually to wrong value. should i instead add every field of
    // ClusterStampNameData to signature??

    @Override
    public byte[] getSignatureMessage(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse) {
        ByteBuffer getClusterStampFileNamesByteBuffer = ByteBuffer.allocate(getByteBufferSize(getClusterStampFileNamesResponse));
        occupyByteArray(getClusterStampFileNamesResponse, getClusterStampFileNamesByteBuffer);
        byte[] getClusterStampFileNamesInBytes = getClusterStampFileNamesByteBuffer.array();
        return CryptoHelper.cryptoHash(getClusterStampFileNamesInBytes).getBytes();
    }

    private int getByteBufferSize(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse){
        int size = 0;
        if(getClusterStampFileNamesResponse.getMajor() != null){
            size += getClusterStampFileNamesResponse.getMajor().getHash().getBytes().length;
        }
        for(ClusterStampNameData tokenClusterStampNameData : getClusterStampFileNamesResponse.getTokenClusterStampNames()){
            size += tokenClusterStampNameData.getHash().getBytes().length;
        }
        size += getClusterStampFileNamesResponse.getSignerHash().getBytes().length;
        return size;
    }

    private void occupyByteArray(GetClusterStampFileNamesResponse getClusterStampFileNamesResponse, ByteBuffer getClusterStampFileNamesByteBuffer){
        if(getClusterStampFileNamesResponse.getMajor() != null){
            getClusterStampFileNamesByteBuffer.put(getClusterStampFileNamesResponse.getMajor().getHash().getBytes());
        }
        for(ClusterStampNameData tokenClusterStampNameData : getClusterStampFileNamesResponse.getTokenClusterStampNames()){
            getClusterStampFileNamesByteBuffer.put(tokenClusterStampNameData.getHash().getBytes());
        }
        getClusterStampFileNamesByteBuffer.put(getClusterStampFileNamesResponse.getSignerHash().getBytes());
    }
}
