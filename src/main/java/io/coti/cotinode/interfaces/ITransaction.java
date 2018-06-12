package io.coti.cotinode.interfaces;

import java.time.ZonedDateTime;

public interface ITransaction {
    public String getTransactionHash();
    public void setTransactionHash(String transactionHash);
    public ITransaction getLeftParent();
    public void setLeftParent(ITransaction leftParent);
    public ITransaction getRightParent();
    public void setRightParent(ITransaction rightParent);
    public int getMyWeight();
    public void setMyWeight(int myWeight);
    public int getTotalWeight();
    public void setTotalWeight(int myWeight);
    public boolean getIsTreshHoledAchieved();
    public void setIsTreshHoledAchieved(boolean isthreshold);

    ZonedDateTime getCreateDateTime();

    void setCreateDateTime(ZonedDateTime createDateTime);

    public ZonedDateTime getUpdateDateTime();
    public void setUpdateDateTime(ZonedDateTime updateDateTime);
    public ZonedDateTime getAttachmentDateTime();
    public void setAttachmentDateTime(ZonedDateTime attachmentDateTime);
    public ZonedDateTime getPowStartDateTime();
    public void setPowStartDateTime(ZonedDateTime powStartDateTime);
    public ZonedDateTime getEndDateTime();
    public void setEndDateTime(ZonedDateTime endDateTime);

}