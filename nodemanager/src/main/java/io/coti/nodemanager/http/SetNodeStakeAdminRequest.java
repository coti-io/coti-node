package io.coti.nodemanager.http;

import io.coti.basenode.http.Request;
import io.coti.nodemanager.data.StakingNodeData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class SetNodeStakeAdminRequest extends Request {

    @NotNull
    private @Valid StakingNodeData stakingNodeData;

    private SetNodeStakeAdminRequest() {
    }
}
