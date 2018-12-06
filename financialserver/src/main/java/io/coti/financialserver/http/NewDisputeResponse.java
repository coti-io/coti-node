package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;

public class NewDisputeResponse extends BaseResponse {
    private String disputeHash;

    public NewDisputeResponse(String disputeHash, String status) {
        super(status);
        this.disputeHash = disputeHash;
    }
}
