package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureValidationCrypto;
import io.coti.financialserver.http.data.UploadCMDTokenIconData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;

@Slf4j
@Component
public class UploadCMDTokenIconCrypto extends SignatureValidationCrypto<UploadCMDTokenIconData> {

    @Override
    public byte[] getSignatureMessage(UploadCMDTokenIconData uploadCMDTokenIconData) {

        byte[] userHashInBytes = uploadCMDTokenIconData.getUserHash().getBytes();
        byte[] fileNameInBytes = uploadCMDTokenIconData.getFileName().getBytes();
        byte[] currencyHashInBytes = uploadCMDTokenIconData.getCurrencyHash().getBytes();
        long creationTimeMilli = uploadCMDTokenIconData.getCreationDate().toEpochMilli();

        ByteBuffer uploadTokenIconBuffer = ByteBuffer.allocate(userHashInBytes.length + fileNameInBytes.length + currencyHashInBytes.length + Long.BYTES)
                .put(userHashInBytes).put(fileNameInBytes).put(currencyHashInBytes).putLong(creationTimeMilli);
        return CryptoHelper.cryptoHash(uploadTokenIconBuffer.array()).getBytes();
    }
}
