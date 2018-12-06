package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DisputeItemData implements Serializable {
     private Long id;
     private BigDecimal price;
     private DisputeReason reason;
     private DisputeItemStatus status;
     private List<Hash> disputeDocumentHashes;
     private List<Hash> disputeCommentHashes;

     public DisputeItemData() {
         disputeDocumentHashes = new ArrayList<>();
         disputeCommentHashes = new ArrayList<>();
     }

     public long getId() {
          return id;
     }

     public BigDecimal getPrice() {
          return price;
     }

     public DisputeReason getReason() {
          return reason;
     }

     public DisputeItemStatus getStatus() {
          return status;
     }

     public List<Hash> getDisputeDocumentHashes() {
          return disputeDocumentHashes;
     }

     public List<Hash> getDisputeCommentHashes() {
          return disputeCommentHashes;
     }

     public void addDocumentHash(Hash documentHash) {
         disputeDocumentHashes.add(documentHash);
     }
}
