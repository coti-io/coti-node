package io.coti.cotinode.model;

import ch.qos.logback.core.html.IThrowableRenderer;
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
    private boolean totalTrustScore;
    private Date createTime;
    private Date updateTime;
    private Date attachmentTime;
    private Date processStartTime;
    private Date processEndTime;
    private Date powStartTime;
    private Date powEndTime;
    private int baseTransactionsCount;
    private boolean senderTrustScore;
    private List<ITransaction> baseTransactions;
    private byte[] senderNodeHash;
    private String senderNodeIpAddress;
    private byte[] userHash;

    public boolean isSource(){
        return leftParent == null && rightParent == null;
    }

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
