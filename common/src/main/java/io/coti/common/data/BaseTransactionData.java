package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;
import java.math.BigDecimal;

@Data
public class BaseTransactionData implements IEntity {


    private transient Hash hash;


    private Hash addressHash;
    private BigDecimal amount;
    private Hash transactionHash;
    private int indexInTransactionsChain;
    private Date createTime =null;
    private SignatureData signatureData;
    private BaseTransactionData(){}


    public BaseTransactionData(Hash addressHash, BigDecimal amount, Hash baseTransactionhash, SignatureData signature , Date createTime ){
        this.addressHash = addressHash;

        this.amount = amount;
        this.hash = baseTransactionhash;
        this.signatureData = signature;
        this.createTime = createTime;
    }





    public boolean isSignatureExists(){
        return signatureData !=null;
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
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