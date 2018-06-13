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
    private List<byte[]> trustChain;
    private boolean transactionConsensus;
    private boolean dspConsensus;
    private int totalTrustScore;
    private Date createTime;
    private Date updateTime;
    private Date attachmentTime;
    private Date processStartTime;
    private Date processEndTime;
    private Date powStartTime;
    private Date powEndTime;
    private int baseTransactionsCount;
    private int senderTrustScore;
    private List<ITransaction> baseTransactions;
    private byte[] senderNodeHash;
    private String senderNodeIpAddress;
    private byte[] userHash;
    private List<byte[]> childrenTransactions;

    @Override
    public void setAttachmentTime(Date attachmentTime) {
        this.attachmentTime = attachmentTime;
    }

    public boolean isSource(){
        return childrenTransactions == null || childrenTransactions.size() == 0;
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
        return createTime;
    }

    @Override
    public boolean isThresholdAchieved() {
        return transactionConsensus;
    }

    @Override
    public int getTotalTrustScore() {
        return totalTrustScore;
    }

    @Override
    public int getSenderTrustScore() {
        return senderTrustScore;
    }

    @Override
    public void setTotalTrustScore(int totalTrustScore) {
        this.totalTrustScore = totalTrustScore;
    }

    @Override
    public void setThresholdAchieved(boolean isAchieved) {
        transactionConsensus = isAchieved;
    }

    @Override
    public void setChildrenTransactions(List<byte[]> childrenTransactions) {
        this.childrenTransactions = childrenTransactions;
    }

    @Override
    public byte[] getHash() {
        return new byte[0];
    }

    @Override
    public ITransaction getLeftParent() {
        return leftParent;
    }

    @Override
    public ITransaction getRightParent() {
        return rightParent;
    }

    @Override
    public void setPowStartTime(Date powStartTime) {
        this.powStartTime = powStartTime;
    }

    @Override
    public void setPowEndTime(Date powEndTime) {
        this.powEndTime = powEndTime;
    }

    @Override
    public void setProcessStartTime(Date processStartTime) {
        this.processStartTime = processStartTime;
    }

    @Override
    public void setProcessEndTime(Date processEndTime) {
        this.processEndTime = processEndTime;
    }


}
