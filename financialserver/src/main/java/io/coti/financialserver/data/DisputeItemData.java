package io.coti.financialserver.data;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
public class DisputeItemData implements Serializable, ISignable, ISignValidatable {

    private Hash userHash;
    @NotNull
    private Long id;
    private BigDecimal price;
    @NotNull
    private DisputeReason reason;
    private Hash disputeHash;
    private DisputeItemStatus status;
    private List<Hash> disputeDocumentHashes;
    private List<Hash> disputeCommentHashes;
    private SignatureData userSignature;

    public DisputeItemData() {
     disputeDocumentHashes = new ArrayList<>();
     disputeCommentHashes = new ArrayList<>();
    }

    public void addDocumentHash(Hash documentHash) {
     disputeDocumentHashes.add(documentHash);
    }

    public void addCommentHash(Hash commentHash) {
        disputeCommentHashes.add(commentHash);
    }

    @Override
    public SignatureData getSignature() {
    return userSignature;
    }

    @Override
    public Hash getSignerHash() {
    return userHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
    userHash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
    this.userSignature = signature;
    }
}
