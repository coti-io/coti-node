package io.coti.financialserver.http.data;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.DisputeItemStatus;
import io.coti.financialserver.data.DisputeReason;

@Data
public class DisputeItemResponseData {
    private Long id;
    private BigDecimal price;
    private DisputeReason reason;
    private DisputeItemStatus status;
    private List<String> disputeDocumentHashes;
    private List<String> disputeCommentHashes;

    public DisputeItemResponseData(DisputeItemData disputeItemData) {
        this.id = disputeItemData.getId();
        this.price = disputeItemData.getPrice();
        this.reason = disputeItemData.getReason();
        this.disputeCommentHashes = new ArrayList<>();
        disputeItemData.getDisputeDocumentHashes().forEach(hash -> disputeDocumentHashes.add(hash.toString()));
        this.disputeCommentHashes = new ArrayList<>();
        disputeItemData.getDisputeCommentHashes().forEach(hash -> disputeCommentHashes.add(hash.toString()));
    }

}
