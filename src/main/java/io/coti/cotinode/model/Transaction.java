package io.coti.cotinode.model;

import io.coti.cotinode.interfaces.ITransaction;

import java.time.ZonedDateTime;


public class Transaction implements ITransaction {

    private String TransactionHash;
    private ITransaction LeftParent;
    private ITransaction rightParent;
    private int myTrustScore;
    private int totalTrustScore;
    private boolean isApproved;
    private boolean isTreshHoledAchieved;
    private ZonedDateTime CreateDateTime;
    private ZonedDateTime UpdateDateTime;
    private ZonedDateTime AttachmentDateTime;
    private ZonedDateTime PowStartDateTime;
    private ZonedDateTime EndDateTime;

    @Override
    public String getTransactionHash() {
        return TransactionHash;
    }

    @Override
    public void setTransactionHash(String transactionHash) {
        TransactionHash = transactionHash;
    }

    @Override
    public ITransaction getLeftParent() {
        return LeftParent;
    }

    @Override
    public void setLeftParent(ITransaction leftParent) {
        LeftParent = leftParent;
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
    public void setTotalWeight(int myWeight) {
        this.myTrustScore = myWeight;
    }

    @Override
    public int getTotalWeight() {
        return myTrustScore;
    }

    @Override
    public void setMyWeight(int myWeight) {
        this.myTrustScore = myWeight;
    }

    @Override
    public boolean getIsApproved() {
        return isApproved;
    }

    @Override
    public void setApprovalsNumber(boolean isApproved) {
        this.isApproved = isApproved;
    }

    @Override
    public boolean getIsTreshHoledAchieved() {
        return isTreshHoledAchieved;
    }

    @Override
    public void setIsTreshHoledAchieved(boolean isthreshold) {
        this.isTreshHoledAchieved = isthreshold;
    }

    @Override
    public ZonedDateTime getCreateDateTime() { return CreateDateTime; }

    @Override
    public void setCreateDateTime(ZonedDateTime createDateTime) { CreateDateTime = createDateTime; }

    @Override
    public ZonedDateTime getUpdateDateTime() {
        return UpdateDateTime;
    }

    @Override
    public void setUpdateDateTime(ZonedDateTime updateDateTime) {
        UpdateDateTime = updateDateTime;
    }

    @Override
    public ZonedDateTime getAttachmentDateTime() {
        return AttachmentDateTime;
    }

    @Override
    public void setAttachmentDateTime(ZonedDateTime attachmentDateTime) {
        AttachmentDateTime = attachmentDateTime;
    }

    @Override
    public ZonedDateTime getPowStartDateTime() {
        return PowStartDateTime;
    }

    @Override
    public void setPowStartDateTime(ZonedDateTime powStartDateTime) {
        PowStartDateTime = powStartDateTime;
    }

    @Override
    public ZonedDateTime getEndDateTime() {
        return EndDateTime;
    }

    @Override
    public void setEndDateTime(ZonedDateTime endDateTime) {
        EndDateTime = endDateTime;
    }

}

