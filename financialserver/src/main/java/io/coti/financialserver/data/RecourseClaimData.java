package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class RecourseClaimData implements IEntity, ISignable, ISignValidatable, IPropagatable {

    @NotNull
    Hash merchantHash;
    @NotNull
    List<Hash> transactionHashes;
    @NotNull
    List<Hash> disputeHashes;
    BigDecimal amountToPay;
    private SignatureData merchantSignature;

    public RecourseClaimData() {
        disputeHashes = new ArrayList<>();
        amountToPay = new BigDecimal(0);
    }

    @Override
    public Hash getHash() {
        return merchantHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.merchantHash = hash;
    }

    @Override
    public SignatureData getSignature() {
        return merchantSignature;
    }

    @Override
    public Hash getSignerHash() {
        return merchantHash;
    }

    @Override
    public void setSignerHash(Hash hash) {
        merchantHash = hash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        merchantSignature = signature;
    }
}
