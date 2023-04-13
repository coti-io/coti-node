package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MintingFeeQuoteData implements ISignable, ISignValidatable, Serializable {

    @NotNull
    private Instant createTime;
    @DecimalMin(value = "0")
    protected BigDecimal mintingAmount;
    @NotNull
    protected @Valid Hash currencyHash;
    @DecimalMin(value = "0")
    protected BigDecimal mintingFee;
    private Hash signerHash;
    @NotNull
    private @Valid SignatureData signatureData;

    public MintingFeeQuoteData() {
    }

    public MintingFeeQuoteData(Hash currencyHash, Instant createTime, BigDecimal mintingAmount, BigDecimal mintingFee) {
        this.createTime = createTime;
        this.currencyHash = currencyHash;
        this.mintingAmount = mintingAmount;
        this.mintingFee = mintingFee;
    }

    @Override
    public SignatureData getSignature() {
        return this.signatureData;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signatureData = signature;
    }
}
