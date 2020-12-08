package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM,
        property = "name")
@JsonTypeIdResolver(BaseTransactionDataResolver.class)
public abstract class BaseTransactionData implements Serializable {

    private static final long serialVersionUID = 4812274089984863159L;
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

    protected BaseTransactionData(Hash addressHash, BigDecimal amount, Instant createTime) {
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

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash.getBytes());
    }

    public void setSignature(SignatureData signatureData) {
        this.signatureData = signatureData;
    }

}