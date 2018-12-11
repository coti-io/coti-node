package io.coti.financialserver.data;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.coti.basenode.data.Hash;

@Data
public class DisputeItemData implements Serializable {
     @NotNull
     private Long id;
     private BigDecimal price;
     @NotNull
     private DisputeReason reason;
     private DisputeItemStatus status;
     private List<Hash> disputeDocumentHashes;
     private List<Hash> disputeCommentHashes;

     public DisputeItemData() {
         disputeDocumentHashes = new ArrayList<>();
         disputeCommentHashes = new ArrayList<>();
     }

     public void addDocumentHash(Hash documentHash) {
         disputeDocumentHashes.add(documentHash);
     }
}
