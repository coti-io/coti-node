package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class RecourseClaimData implements ISignable, ISignValidatable, IPropagatable {

    private static final long serialVersionUID = 5881994559179493495L;
    @NotNull
    private Hash merchantHash;
    @NotNull
    private List<Hash> transactionHashes;
    @NotNull
    private List<Hash> disputeHashes;
    private BigDecimal amountToPay;
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
    public void setSignature(SignatureData signature) {
        merchantSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return getHash();
    }

    @Override
    public void setSignerHash(Hash hash) {
        merchantHash = hash;
    }
}
