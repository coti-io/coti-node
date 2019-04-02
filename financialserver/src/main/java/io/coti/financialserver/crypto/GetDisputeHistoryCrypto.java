package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.data.GetDisputeHistoryData;
import org.springframework.stereotype.Component;

@Component
public class GetDisputeHistoryCrypto extends SignatureCrypto<GetDisputeHistoryData> {
    @Override
    public byte[] getSignatureMessage(GetDisputeHistoryData getDisputeHistoryData) {
        return getDisputeHistoryData.getDisputeHash().getBytes();
    }
}
