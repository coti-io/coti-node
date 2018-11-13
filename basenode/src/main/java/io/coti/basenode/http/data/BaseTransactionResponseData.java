package io.coti.basenode.http.data;


import io.coti.basenode.data.BaseTransactionData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BaseTransactionResponseData {

    protected String hash;
    protected String addressHash;
    protected BigDecimal amount;
    protected Date createTime;

    protected BaseTransactionResponseData() {

    }

    public BaseTransactionResponseData(BaseTransactionData baseTransactionData) {
        this.hash = baseTransactionData.getHash() == null ? null : baseTransactionData.getHash().toHexString();
        this.addressHash = baseTransactionData.getAddressHash().toHexString();
        this.amount = baseTransactionData.getAmount();
        this.createTime = baseTransactionData.getCreateTime();

    }
}
