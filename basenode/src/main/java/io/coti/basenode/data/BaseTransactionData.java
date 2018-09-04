package io.coti.basenode.data;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class BaseTransactionData implements Serializable {
    @NotNull
    private Hash hash;
    @NotNull
    private Hash addressHash;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Date createTime;
    private SignatureData signatureData;

    private BaseTransactionData() {
    }

    public BaseTransactionData(Hash addressHash, BigDecimal amount, Hash baseTransactionhash, SignatureData signature, Date createTime) {
        this.addressHash = addressHash;

        this.amount = amount;
        this.hash = baseTransactionhash;
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