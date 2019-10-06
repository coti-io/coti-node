package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SetNodeStakeResponse extends BaseResponse {

    private String nodeHash;
    private String stake;

    public SetNodeStakeResponse(Hash nodeHash, BigDecimal stake) {
        this.nodeHash = nodeHash.toHexString();
        this.stake = stake.toString();
    }
}
