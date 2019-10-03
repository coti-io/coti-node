package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class SetNodeStakeAdminRequest extends Request {

    @NotNull
    private Hash nodeHash;
    @NotNull
    private BigDecimal stake;

    public SetNodeStakeAdminRequest(){
    }

    public SetNodeStakeAdminRequest(Hash nodeHash, BigDecimal stake) {
        this.nodeHash = nodeHash;
        this.stake = stake;
    }
}
