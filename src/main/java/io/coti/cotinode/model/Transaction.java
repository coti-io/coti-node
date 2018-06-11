package io.coti.cotinode.model;

import io.coti.cotinode.model.Interfaces.IEntity;

import java.util.Date;

public class Transaction implements IEntity {
    private byte[] hash;
    private Transaction source1;
    private Transaction source2;
    // TCC - Transaction Consensus
    //DSPC - DSP Consensus
    Date transactionConsensusTimestamp;


    private String name;
    public Transaction(String name){
        this.name = name;
    }

    public Transaction(byte[] nameBytes){
        this.name = new String(nameBytes);
    }

    @Override
    public byte[] getKey() {
        return name.getBytes();
    }

    @Override
    public byte[] getBytes() {
        return name.getBytes();
    }

    @Override
    public String toString(){
        return name;
    }

    @Override
    public boolean equals(Object other){
        if (other == this){
            return true;
        }

        if(!(other instanceof Transaction)){
            return false;
        }

        return name.equals(((Transaction) other).name);
    }
}
