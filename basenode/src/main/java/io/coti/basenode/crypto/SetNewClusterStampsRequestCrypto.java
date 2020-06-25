package io.coti.basenode.crypto;

import io.coti.basenode.http.SetNewClusterStampsRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class SetNewClusterStampsRequestCrypto extends SignatureValidationCrypto<SetNewClusterStampsRequest> {

    @Override
    public byte[] getSignatureMessage(SetNewClusterStampsRequest setNewClusterStampsRequest) {
        byte[] folderPathInBytes = setNewClusterStampsRequest.getFolderPath().getBytes();
        byte[] clusterStampFileNameInBytes = setNewClusterStampsRequest.getClusterStampFileName().getBytes();

        ByteBuffer setNewClusterStampsRequestBuffer = ByteBuffer.allocate(folderPathInBytes.length + clusterStampFileNameInBytes.length)
                .put(folderPathInBytes).put(clusterStampFileNameInBytes);
        return CryptoHelper.cryptoHash(setNewClusterStampsRequestBuffer.array()).getBytes();
    }
}
