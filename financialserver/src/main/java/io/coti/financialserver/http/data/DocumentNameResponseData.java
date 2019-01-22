package io.coti.financialserver.http.data;

import io.coti.financialserver.data.ActionSide;
import io.coti.financialserver.data.DisputeDocumentData;
import io.coti.financialserver.http.data.interfaces.IDisputeEventResponseData;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class DocumentNameResponseData implements IDisputeEventResponseData {
    private String hash;
    private String disputeHash;
    private List<Long> itemIds;
    private String fileName;
    private ActionSide uploadSide;
    private Instant creationTime;

    public DocumentNameResponseData(DisputeDocumentData disputeDocumentData) {
        this.hash = disputeDocumentData.getHash().toString();
        this.disputeHash = disputeDocumentData.getDisputeHash().toString();
        this.itemIds = disputeDocumentData.getItemIds();
        this.fileName = disputeDocumentData.getFileName();
        this.uploadSide = disputeDocumentData.getUploadSide();
        this.creationTime = disputeDocumentData.getCreationTime();
    }
}
