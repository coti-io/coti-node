package io.coti.financialserver.http.data;

import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.DisputeItemStatus;
import io.coti.financialserver.data.DisputeReason;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DisputeItemResponseData {
    private Long id;
    private BigDecimal price;
    private DisputeReason reason;
    private DisputeItemStatus status;
    private List<String> disputeDocumentHashes;
    private List<String> disputeCommentHashes;

    public DisputeItemResponseData(DisputeItemData disputeItemData) {
        disputeDocumentHashes = new ArrayList<>();
        disputeCommentHashes = new ArrayList<>();

        this.id = disputeItemData.getId();
        this.price = disputeItemData.getPrice();
        this.reason = disputeItemData.getReason();
        this.status = disputeItemData.getStatus();
        disputeItemData.getDisputeDocumentHashes().forEach(hash -> disputeDocumentHashes.add(hash.toString()));
        disputeItemData.getDisputeCommentHashes().forEach(hash -> disputeCommentHashes.add(hash.toString()));
    }

}
