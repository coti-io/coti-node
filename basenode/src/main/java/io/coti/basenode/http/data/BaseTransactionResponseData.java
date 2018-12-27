package io.coti.basenode.http.data;


import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.BaseTransactionName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BaseTransactionResponseData {

    private String hash;
    private String addressHash;
    private BigDecimal amount;
    private Date createTime;
    private String name;

    public BaseTransactionResponseData(BaseTransactionData baseTransactionData) {
        this.hash = baseTransactionData.getHash() == null ? null : baseTransactionData.getHash().toHexString();
        this.addressHash = baseTransactionData.getAddressHash().toHexString();
        this.amount = baseTransactionData.getAmount();
        this.createTime = baseTransactionData.getCreateTime();
        this.name = BaseTransactionName.getName(baseTransactionData.getClass()).name();

    }
}
