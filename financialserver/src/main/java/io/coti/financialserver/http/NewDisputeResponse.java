package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class NewDisputeResponse extends BaseResponse {
    private String disputeHash;

    public NewDisputeResponse(String disputeHash, String status) {
        super(status);
        this.disputeHash = disputeHash;
    }
}
