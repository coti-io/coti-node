package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.coti.basenode.data.interfaces.IBaseTransactionType;
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
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InputBaseTransactionType.class),
})
public abstract class BaseTransactionData implements Serializable {
    @NotNull
    protected Hash hash;
    @NotNull
    protected Hash addressHash;
    private BigDecimal amount;
    @NotNull
    protected IBaseTransactionType type;
    @NotNull
    protected Date createTime;
    protected @Valid SignatureData signatureData;

    public BaseTransactionData(Hash addressHash, BigDecimal amount, Hash baseTransactionHash, SignatureData signature, Date createTime) {
        this.addressHash = addressHash;
        this.amount = amount;
        this.hash = baseTransactionHash;
        this.signatureData = signature;
        this.createTime = createTime;
    }

    public BaseTransactionData(Hash addressHash, BigDecimal amount, String type,Date createTime) {
        this.addressHash = addressHash;
        this.createTime = createTime;
        this.setAmount(amount);
        this.setType(type);
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

    public boolean isInput() {
        return this.getAmount().signum() <= 0;
    }

    public IBaseTransactionType getType(String type) {
        return isInput() ? InputBaseTransactionType.valueOf(type) : OutputBaseTransactionType.valueOf(type);
    }

    public void setType(String type) {
        this.type = isInput() ? InputBaseTransactionType.valueOf(type) : OutputBaseTransactionType.valueOf(type);
    }
}