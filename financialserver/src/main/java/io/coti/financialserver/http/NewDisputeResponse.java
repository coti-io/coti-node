package io.coti.financialserver.http;

import lombok.Data;

import io.coti.basenode.http.BaseResponse;

@Data
public class NewDisputeResponse extends BaseResponse {
    private String disputeHash;

    public NewDisputeResponse(String disputeHash, String status) {
        super(status);
        this.disputeHash = disputeHash;
    }
}
