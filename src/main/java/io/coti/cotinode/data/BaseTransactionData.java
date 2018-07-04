package io.coti.cotinode.data;

import io.coti.cotinode.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;

@Data
public class BaseTransactionData implements IEntity {
    private transient Hash hash;
    private String signature;
    private AddressData addressData;
    private Hash addressHash;
    private double amount;
    private Hash transactionHash;
    private int indexInTransactionsChain;
    private BaseTransactionData nextBaseTransactionData;
    private Date createTime;

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

    public BaseTransactionData(String addressHashInput, double amount){
        this.addressHash = new Hash(addressHashInput);
        this.amount = amount;
    }

    public BaseTransactionData(Integer addressHashInput, double amount){
        this.addressHash = new Hash(addressHashInput);
        this.amount = amount;
    }



    public BaseTransactionData(String addressHash, double amount, Hash hash, String signature){
        this(addressHash, amount);
        this.hash = hash;
        this.signature = signature;
    }

    public BaseTransactionData(Integer addressHash, double amount, Hash hash, String signature){
        this(addressHash, amount);
        this.hash = hash;
        this.signature = signature;
    }

    public BaseTransactionData(Hash hash, Double randomDouble, Hash hash1, String signature) {
        this.addressHash = hash;
        this.amount = randomDouble;
        this.hash = hash1;
        this.signature = signature;
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