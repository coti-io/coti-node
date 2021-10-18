package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewDisputeResponse extends BaseResponse {
    private String disputeHash;

    public NewDisputeResponse(String disputeHash, String status) {
        super(status);
        this.disputeHash = disputeHash;
    }
}
