package io.coti.financialserver.http.data;

import io.coti.basenode.http.data.interfaces.IResponseData;
import io.coti.financialserver.data.DistributionEntryStatus;
import io.coti.financialserver.data.Fund;
import io.coti.financialserver.data.FundDistributionData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FundDistributionResponseStateData implements IResponseData {

    private String hash;
    private String hashByDate;
    private String fileName;
    private DistributionEntryStatus status;
    private long id;
    private String receiverAddress;
    private Fund distributionPoolFund;
    private BigDecimal amount;
    private String createTime;
    private String releaseTime;
    private String source;

    public FundDistributionResponseStateData(FundDistributionData fundDistributionData) {
        this.hash = fundDistributionData.getHash().toString();
        this.hashByDate = fundDistributionData.getHashByDate().toString();
        this.fileName = fundDistributionData.getFileName();
        this.status = fundDistributionData.getStatus();
        this.id = fundDistributionData.getId();
        this.receiverAddress = fundDistributionData.getReceiverAddress().toString();
        this.distributionPoolFund = fundDistributionData.getDistributionPoolFund();
        this.amount = fundDistributionData.getAmount();
        this.createTime = fundDistributionData.getCreateTime().toString();
        this.releaseTime = fundDistributionData.getReleaseTime().toString();
        this.source = fundDistributionData.getSource();
    }
}
