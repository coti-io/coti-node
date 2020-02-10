package io.coti.basenode.http.data;


import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public abstract class BaseTransactionResponseData implements IResponseData {

    private String hash;
    private String addressHash;
    private BigDecimal amount;
    private Instant createTime;
    private String name;

    protected BaseTransactionResponseData(BaseTransactionData baseTransactionData) {
        this.hash = baseTransactionData.getHash() == null ? null : baseTransactionData.getHash().toString();
        this.addressHash = baseTransactionData.getAddressHash().toString();
        this.amount = baseTransactionData.getAmount();
        this.createTime = baseTransactionData.getCreateTime();
        this.name = BaseTransactionName.getName(baseTransactionData.getClass()).name();

    }
}
