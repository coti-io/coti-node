package io.coti.basenode.crypto;

import io.coti.basenode.http.SetNewClusterStampsRequest;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class SetNewClusterStampsRequestCrypto extends SignatureValidationCrypto<SetNewClusterStampsRequest> {

    @Override
    public byte[] getSignatureMessage(SetNewClusterStampsRequest setNewClusterStampsRequest) {
        byte[] folderPathInBytes = setNewClusterStampsRequest.getFolderPath().getBytes();
        byte[] currencyClusterStampFileNameInBytes = setNewClusterStampsRequest.getCurrencyClusterStampFileName().getBytes();
        byte[] balanceClusterStampFileNameInBytes = setNewClusterStampsRequest.getBalanceClusterStampFileName().getBytes();

        ByteBuffer setNewClusterStampsRequestBuffer = ByteBuffer.allocate(folderPathInBytes.length
                + currencyClusterStampFileNameInBytes.length + balanceClusterStampFileNameInBytes.length)
                .put(folderPathInBytes).put(currencyClusterStampFileNameInBytes).put(balanceClusterStampFileNameInBytes);
        return CryptoHelper.cryptoHash(setNewClusterStampsRequestBuffer.array()).getBytes();
    }
}
