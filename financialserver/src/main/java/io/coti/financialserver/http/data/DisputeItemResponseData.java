package io.coti.financialserver.http.data;

import io.coti.basenode.http.data.interfaces.IResponseData;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.DisputeItemStatus;
import io.coti.financialserver.data.DisputeReason;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DisputeItemResponseData implements IResponseData {

    private Long id;
    private BigDecimal price;
    private int quantity;
    private String name;
    private DisputeReason reason;
    private DisputeItemStatus status;
    private List<String> disputeDocumentHashes;
    private List<String> disputeCommentHashes;

    protected DisputeItemResponseData() {

    }

    public DisputeItemResponseData(DisputeItemData disputeItemData) {

        id = disputeItemData.getId();
        price = disputeItemData.getPrice();
        quantity = disputeItemData.getQuantity();
        name = disputeItemData.getName();
        reason = disputeItemData.getReason();
        status = disputeItemData.getStatus();
        disputeDocumentHashes = new ArrayList<>();
        disputeCommentHashes = new ArrayList<>();
        disputeItemData.getDisputeDocumentHashes().forEach(hash -> disputeDocumentHashes.add(hash.toString()));
        disputeItemData.getDisputeCommentHashes().forEach(hash -> disputeCommentHashes.add(hash.toString()));
    }

}
