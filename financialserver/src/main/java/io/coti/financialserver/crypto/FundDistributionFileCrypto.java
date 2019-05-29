package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.data.FundDistributionFileData;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import java.nio.ByteBuffer;

@Component
public class FundDistributionFileCrypto extends SignatureCrypto<FundDistributionFileData> {

    @Override
    public byte[] getSignatureMessage(FundDistributionFileData fileData) {
        ByteBuffer fileDataBuffer =
                ByteBuffer.allocate(fileData.getMessageByteSize());
        fileData.getSignatureMessage().forEach(byteArray -> fileDataBuffer.put(byteArray));

        byte[] fileDataInBytes = fileDataBuffer.array();
        return CryptoHelper.cryptoHash(fileDataInBytes).getBytes();
    }
}
