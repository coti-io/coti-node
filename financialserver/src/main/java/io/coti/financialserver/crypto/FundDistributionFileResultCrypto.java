package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.data.FundDistributionFileResultData;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;

@Service
public class FundDistributionFileResultCrypto extends SignatureCrypto<FundDistributionFileResultData> {

    @Override
    public byte[] getSignatureMessage(FundDistributionFileResultData fundDistributionFileResultData) {

        ByteBuffer fundDistributionFileResultBuffer =
                ByteBuffer.allocate(fundDistributionFileResultData.getMessageByteSize());
        fundDistributionFileResultData.getSignatureMessage().forEach(byteArray -> fundDistributionFileResultBuffer.put(byteArray));

        byte[] clusterStampInBytes = fundDistributionFileResultBuffer.array();
        return CryptoHelper.cryptoHash(clusterStampInBytes).getBytes();
    }
}
