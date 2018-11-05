package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IBaseTransactionData;
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
public class BaseTransactionData implements Serializable, IBaseTransactionData {
    @NotNull
    protected Hash hash;
    protected OutputBaseTransactionType type;
    @NotNull
    protected Hash addressHash;
    @NotNull
    protected BigDecimal amount;
    @NotNull
    protected Date createTime;
    protected @Valid SignatureData signatureData;

    public BaseTransactionData(Hash addressHash, BigDecimal amount, Hash baseTransactionHash, SignatureData signature, Date createTime) {
        this.addressHash = addressHash;
        this.amount = amount;
        //    this.type = OutputBaseTransactionType.valueOf(type);
        this.hash = baseTransactionHash;
        log.info("Construct {}", baseTransactionHash);
        this.signatureData = signature;
        this.createTime = createTime;
    }

    public BaseTransactionData(Hash addressHash, BigDecimal amount, Date createTime) {
        this.addressHash = addressHash;
        this.amount = amount;
        this.createTime = createTime;
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