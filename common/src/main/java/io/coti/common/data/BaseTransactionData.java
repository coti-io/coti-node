package io.coti.common.data;

import io.coti.common.data.AddressData;
import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;
import java.math.BigDecimal;

@Data
public class BaseTransactionData implements IEntity {
    private transient Hash hash;

    private AddressData addressData;
    private Hash addressHash;
    private BigDecimal amount;
    private Hash transactionHash;
    private int indexInTransactionsChain;
    private BaseTransactionData nextBaseTransactionData;
    private Date createTime;
    private SignatureData signatureData;
    private BaseTransactionData(){}


    public Hash getAddressHash()
    {
        return addressHash;
    }

    public int getIndexInTransactionsChain()
    {
        return indexInTransactionsChain;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public BaseTransactionData(String addressHashInput, BigDecimal amount){
        this.addressHash = new Hash(addressHashInput);
        this.amount = amount;
    }

    public BaseTransactionData(Hash hash, BigDecimal amount){
        this.addressHash = hash;
        this.amount = amount;
    }

    public BaseTransactionData(Integer addressHashInput, BigDecimal amount){
        this.addressHash = new Hash(addressHashInput);
        this.amount = amount;
    }

    public BaseTransactionData(String addressHash, BigDecimal amount, Hash hash, SignatureData signature){
        this(addressHash, amount);
        this.hash = hash;
        this.signatureData = signature;
    }

    public BaseTransactionData(String addressHash, BigDecimal amount, Hash hash, Date createTime ,SignatureData signature){
        this(addressHash, amount);
        this.hash = hash;
        this.createTime = createTime;
        this.signatureData = signature;
    }

    public BaseTransactionData(Integer addressHash, BigDecimal amount, Hash hash, SignatureData signature){
        this(addressHash, amount);
        this.hash = hash;
        this.signatureData = signature;
    }

    public BaseTransactionData(Hash hash, BigDecimal randomDouble, Hash hash1, SignatureData signature) {
        this.addressHash = hash;
        this.amount = randomDouble;
        this.hash = hash1;
        this.signatureData = signature;
    }


    public boolean isSignatureExists(){
        return signatureData !=null;
    }

    @Override
    public Hash getKey() {
        return hash;
    }

    @Override
    public void setKey(Hash hash) {
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