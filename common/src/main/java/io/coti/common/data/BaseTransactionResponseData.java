package io.coti.common.data;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BaseTransactionResponseData {


    private Hash addressHash;
    private BigDecimal amount;
    private Date createTime;


    public BaseTransactionResponseData(BaseTransactionData baseTransactionData){
        this.addressHash = baseTransactionData.getAddressHash();
        this.amount = baseTransactionData.getAmount();
        this.createTime = baseTransactionData.getCreateTime();

    }
}
