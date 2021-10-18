package io.coti.nodemanager.http;

import io.coti.basenode.http.interfaces.IRequest;
import io.coti.nodemanager.data.StakingNodeData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class SetNodeStakeAdminRequest implements IRequest {

    @NotNull
    private @Valid StakingNodeData stakingNodeData;

    private SetNodeStakeAdminRequest() {
    }
}
