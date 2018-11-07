package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Slf4j
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM,
        include = JsonTypeInfo.As.PROPERTY,
        property = "name")
@JsonTypeIdResolver(BaseTransactionDataResolver.class)
public abstract class BaseTransactionData implements Serializable {
    @NotNull
    protected Hash hash;
    @NotNull
    protected Hash addressHash;
    private BigDecimal amount;
    @NotNull
    protected Date createTime;
    protected @Valid SignatureData signatureData;

    public BaseTransactionData(Hash addressHash, BigDecimal amount, Hash baseTransactionHash, SignatureData signature, Date createTime) {
        this.addressHash = addressHash;
        this.setAmount(amount);
        this.hash = baseTransactionHash;
        this.signatureData = signature;
        this.createTime = createTime;
    }

    public BaseTransactionData(Hash addressHash, BigDecimal amount, Date createTime) {
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
}