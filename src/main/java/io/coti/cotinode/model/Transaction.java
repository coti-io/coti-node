package io.coti.cotinode.model;

import io.coti.cotinode.model.Interfaces.IEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Transaction implements IEntity {
    private byte[] hash;
    private Transaction sourceTransaction1;
    private Transaction sourceTransaction2;
    private List<Transaction> trustChain;
    private boolean transactionConcensus;
    private boolean dspConcensus;

    // TCC - Transaction Consensus
    //DSPC - DSP Consensus
    Date transactionConsensusTimestamp;

    public Transaction(byte[] hash){
        this.hash = hash;
    }

    @Override
    public byte[] getKey() {
        return hash;
    }

    @Override
    public String toString(){
        return String.valueOf(hash);
    }

    @Override
    public boolean equals(Object other){
        if (other == this){
            return true;
        }

        if(!(other instanceof Transaction)){
            return false;
        }
        return Arrays.equals(hash, ((Transaction) other).getKey());
    }
}
