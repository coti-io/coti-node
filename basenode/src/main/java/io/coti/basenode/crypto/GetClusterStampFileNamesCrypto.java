package io.coti.basenode.crypto;

import io.coti.basenode.http.GetClusterStampFileNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
@Slf4j // TODO remove
public class GetClusterStampFileNamesCrypto extends SignatureCrypto<GetClusterStampFileNames>{


    @Override
    public byte[] getSignatureMessage(GetClusterStampFileNames getClusterStampFileNames) {
        //TODO 8/13/2019 astolia: need to sign the hash as i did? or is it redundant?
        ByteBuffer getClusterStampFileNamesByteBuffer = ByteBuffer.allocate(getByteBufferSize(getClusterStampFileNames));
        occupyByteArray(getClusterStampFileNames, getClusterStampFileNamesByteBuffer);
        byte[] getClusterStampFileNamesInBytes = getClusterStampFileNamesByteBuffer.array();
        return CryptoHelper.cryptoHash(getClusterStampFileNamesInBytes).getBytes();
    }

    private int getByteBufferSize(GetClusterStampFileNames getClusterStampFileNames){
        int size = 0;
        if(getClusterStampFileNames.getMajor() != null){
            size += getClusterStampFileNames.getMajor().length();
        }
        for(String token : getClusterStampFileNames.getTokens()){
            log.info("token.getBytes().length: {} , token.length(): {}",token.getBytes().length,token.length()); // TODO remove
            size += token.getBytes().length;
        }
        size += getClusterStampFileNames.getSignerHash().getBytes().length;
        return size;
    }

    private void occupyByteArray(GetClusterStampFileNames getClusterStampFileNames, ByteBuffer getClusterStampFileNamesByteBuffer){
        if(getClusterStampFileNames.getMajor() != null){
            getClusterStampFileNamesByteBuffer.put(getClusterStampFileNames.getMajor().getBytes());
        }
        for(String token : getClusterStampFileNames.getTokens()){
            getClusterStampFileNamesByteBuffer.put(token.getBytes());
        }
        getClusterStampFileNamesByteBuffer.put(getClusterStampFileNames.getSignerHash().getBytes());
    }
}
