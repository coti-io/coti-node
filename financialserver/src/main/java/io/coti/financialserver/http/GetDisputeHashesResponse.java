package io.coti.financialserver.http;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;

@Data
public class GetDisputeHashesResponse extends BaseResponse {

    private List<String> disputeHashes;

    public GetDisputeHashesResponse( List<Hash> disputeHashes) {
        super();
        this.disputeHashes = new ArrayList<>();
        disputeHashes.forEach(hash -> this.disputeHashes.add(hash.toString()));
    }
}
