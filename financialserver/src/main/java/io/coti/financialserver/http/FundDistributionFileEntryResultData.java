package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.Fund;
import lombok.Data;

@Data
public class FundDistributionFileEntryResultData {

    private Hash receiverAddress;
    private String distributionPool;
    private String source;
    private boolean accepted;
    private boolean uniqueByDate;
    private boolean passedPreBalanceCheck;


    public FundDistributionFileEntryResultData(Hash receiverAddress, String distributionPool, String source, boolean accepted, boolean passedPreBalanceCheck, boolean uniqueByDate) {
        this.receiverAddress = receiverAddress;
        this.distributionPool = distributionPool;
        this.source = source;
        this.accepted = accepted;
        this.uniqueByDate = uniqueByDate;
        this.passedPreBalanceCheck = passedPreBalanceCheck;
    }

}
