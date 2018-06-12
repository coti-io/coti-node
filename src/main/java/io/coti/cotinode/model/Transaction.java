package io.coti.cotinode.model;

import io.coti.cotinode.interfaces.ITransaction;

import java.time.ZonedDateTime;
import java.util.List;


public class Transaction implements ITransaction {

    private String hash;
    private ITransaction leftParent;
    private ITransaction rightParent;
    private boolean tcc;
    
    public List<String> getTrustChain() {
        return trustChain;
    }

    public void setTrustChain(List<String> trustChain) {
        this.trustChain = trustChain;
    }
    
    private List<String> trustChain;
    private int myTrustScore;
    private int totalTrustScore;
    
    private ZonedDateTime createDateTime;
    private ZonedDateTime updateDateTime;
    private ZonedDateTime attachmentDateTime;
    private ZonedDateTime powStartDateTime;
    private ZonedDateTime endDateTime;

    @Override
    public String getTransactionHash() {
        return hash;
    }

    @Override
    public void setTransactionHash(String transactionHash) {
        this.hash = transactionHash;
    }

    @Override
    public ITransaction getLeftParent() {
        return leftParent;
    }

    @Override
    public void setLeftParent(ITransaction leftParent) {
        this.leftParent = leftParent;
    }

    @Override
    public ITransaction getRightParent() {
        return rightParent;
    }

    @Override
    public void setRightParent(ITransaction rightParent) {
        this.rightParent = rightParent;
    }

    @Override
    public int getMyWeight() {
        return myTrustScore;
    }

    @Override
    public void setMyWeight(int myWeight) {
        this.myTrustScore = myWeight;
    }

    @Override
    public void setTotalWeight(int myWeight) {
        this.totalTrustScore = myWeight;
    }

    @Override
    public int getTotalWeight() {
        return totalTrustScore;
    }


    @Override
    public boolean getIsTreshHoledAchieved() {
        return tcc;
    }

    @Override
    public void setIsTreshHoledAchieved(boolean isthreshold) {
        this.tcc = isthreshold;
    }

    @Override
    public ZonedDateTime getCreateDateTime() { return createDateTime; }

    @Override
    public void setCreateDateTime(ZonedDateTime createDateTime) { this.createDateTime = createDateTime; }

    @Override
    public ZonedDateTime getUpdateDateTime() {
        return updateDateTime;
    }

    @Override
    public void setUpdateDateTime(ZonedDateTime updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    @Override
    public ZonedDateTime getAttachmentDateTime() {
        return attachmentDateTime;
    }

    @Override
    public void setAttachmentDateTime(ZonedDateTime attachmentDateTime) {
        this.attachmentDateTime = attachmentDateTime;
    }

    @Override
    public ZonedDateTime getPowStartDateTime() {
        return powStartDateTime;
    }

    @Override
    public void setPowStartDateTime(ZonedDateTime powStartDateTime) {
        this.powStartDateTime = powStartDateTime;
    }

    @Override
    public ZonedDateTime getEndDateTime() {
        return endDateTime;
    }

    @Override
    public void setEndDateTime(ZonedDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

}

