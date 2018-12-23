package io.coti.financialserver.http.data;

import io.coti.financialserver.data.DisputeDocumentData;
import lombok.Data;

@Data
public class GetDocumentNameResponseData {
    String hash;
    String fileName;

    public GetDocumentNameResponseData(DisputeDocumentData disputeDocumentData) {
        this.hash = disputeDocumentData.getHash().toString();
        this.fileName = disputeDocumentData.getFileName();
    }
}
