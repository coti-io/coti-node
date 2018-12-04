package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class DisputeItemData implements Serializable {
     private long id;
     private BigDecimal price;
     private DisputeReason reason;
     private DisputeItemStatus status;
     private List<Hash> disputeDocumentHashes;
     private List<Hash> disputeCommentHashes;


}
