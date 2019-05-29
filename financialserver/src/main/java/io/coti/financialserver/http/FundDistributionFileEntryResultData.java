package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class FundDistributionFileEntryResultData {

    private long id;
    private Hash receiverAddress;
    private String distributionPool;
    private String source;
    private boolean accepted;
    private String status;



    public FundDistributionFileEntryResultData(long id, Hash receiverAddress, String distributionPool, String source,
                                               boolean accepted, String status) {
        this.id = id;
        this.receiverAddress = receiverAddress;
        this.distributionPool = distributionPool;
        this.source = source;
        this.accepted = accepted;
        this.status = status;
    }

}
