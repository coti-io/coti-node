package io.coti.financialserver.http.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class FundDistributionFileEntryResultData {

    private long id;
    private String receiverAddress;
    private String distributionPool;
    private String source;
    private boolean accepted;
    private String status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String transactionHash;

    public FundDistributionFileEntryResultData(long id, String receiverAddress, String distributionPool, String source,
                                               boolean accepted, String status) {
        this.id = id;
        this.receiverAddress = receiverAddress;
        this.distributionPool = distributionPool;
        this.source = source;
        this.accepted = accepted;
        this.status = status;
    }

}
