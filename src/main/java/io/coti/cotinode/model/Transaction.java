package io.coti.cotinode.model;

import io.coti.cotinode.model.Interfaces.ITransaction;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Transaction implements ITransaction {
    private byte[] hash;
    private ITransaction leftParent;
    private ITransaction rightParent;
    private List<Transaction> trustChain;
    private boolean transactionConsensus;
    private boolean dspConsensus;
    private boolean senderTrustScore;
    private boolean totalTrustScore;
    private Date createDateTime;
    private Date updateDateTime;
    private Date attachmentDateTime;
    private Date powStartDateTime;
    private Date endDateTime;

    public boolean isSource(){
        return leftParent == null && rightParent == null;
    }

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

    @Override
    public void attachToSource(ITransaction source){
        if (leftParent == null){
            leftParent = source;
        }
        else if(rightParent == null){
            rightParent = source;
        }
        else{
            System.out.println("Unable to attach to source, both parents are full");
            throw new RuntimeException("Unable to attach to source.");
        }
    }

    @Override
    public Date getCreateDateTime() {
        return null;
    }

    @Override
    public boolean isThresholdAchieved() {
        return false;
    }

    @Override
    public int getTotalTrustScore() {
        return 0;
    }

    @Override
    public int getSenderTrustScore() {
        return 0;
    }

    @Override
    public void setTotalTrustScore(int totalTrustScore) {

    }

    @Override
    public void setThresholdAchieved(boolean isAchieved) {

    }

    @Override
    public byte[] getHash() {
        return new byte[0];
    }

    @Override
    public ITransaction getLeftParent() {
        return null;
    }

    @Override
    public ITransaction getRightParent() {
        return null;
    }
}
