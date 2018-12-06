package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.financialserver.data.DisputeItemData;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class NewDisputeRequest implements ISignable, ISignValidatable {

    @NotNull
    private Hash userHash;

    @NotNull
    private Hash transactionHash;

    @NotNull
    private BigDecimal amount; // TODO: should be calculated

    @NotNull
    private List<DisputeItemData> disputeItems;

    @NotNull
    private SignatureData signature;

    public Hash getUserHash() {
        return userHash;
    }

    public Hash getTransactionHash() {
        return transactionHash;
    }

    public SignatureData getSignature() {
        return signature;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public List<DisputeItemData> getDisputeItems() {
        return disputeItems;
    }

    public Hash getSignerHash() {
        return userHash;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setSignerHash(Hash signerHash) {
        userHash = signerHash;
    }

    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }
}
