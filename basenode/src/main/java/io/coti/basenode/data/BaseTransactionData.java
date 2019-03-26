package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM,
        include = JsonTypeInfo.As.PROPERTY,
        property = "name")
@JsonTypeIdResolver(BaseTransactionDataResolver.class)
public abstract class BaseTransactionData implements Serializable {
    @NotNull
    protected Hash hash;
    @NotNull
    protected Hash addressHash;
    protected BigDecimal amount;
    @NotNull
    protected Instant createTime;
    protected @Valid SignatureData signatureData;

    protected BaseTransactionData() {

    }

    public BaseTransactionData(Hash addressHash, BigDecimal amount, Instant createTime) {
        this.addressHash = addressHash;
        this.createTime = createTime;
        this.setAmount(amount);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof BaseTransactionData)) {
            return false;
        }
        return hash.equals(((BaseTransactionData) other).hash);
    }

    public void setSignature(SignatureData signatureData) {
        this.signatureData = signatureData;
    }

    public boolean isOutput() {
        return amount.signum() > 0;
    }

    public boolean isInput() {
        return amount.signum() <= 0;
    }
}