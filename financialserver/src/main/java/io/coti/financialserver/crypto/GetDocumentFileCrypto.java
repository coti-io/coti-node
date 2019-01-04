package io.coti.financialserver.crypto;

import io.coti.basenode.crypto.SignatureCrypto;
import io.coti.financialserver.http.data.GetDocumentFileData;
import org.springframework.stereotype.Service;

@Service
public class GetDocumentFileCrypto extends SignatureCrypto<GetDocumentFileData> {

    @Override
    public byte[] getSignatureMessage(GetDocumentFileData getDocumentFileData) {
        return getDocumentFileData.getDocumentHash().getBytes();
    }
}
