package io.coti.common.http.data;


import io.coti.common.data.BaseTransactionData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BaseTransactionResponseData {

    private String hash;
    private String addressHash;
    private BigDecimal amount;
    private Date createTime;

    public BaseTransactionResponseData(BaseTransactionData baseTransactionData) {
        this.hash = baseTransactionData.getHash() == null ? null : baseTransactionData.getHash().toHexString();
        this.addressHash = baseTransactionData.getAddressHash().toHexString();
        this.amount = baseTransactionData.getAmount();
        this.createTime = baseTransactionData.getCreateTime();

    }
}
