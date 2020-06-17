package io.coti.nodemanager.http;


import io.coti.basenode.http.BaseResponse;
import io.coti.nodemanager.http.data.StakingNodeResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetStakerListResponse extends BaseResponse {

    private List<StakingNodeResponseData> nodes;

    public GetStakerListResponse(List<StakingNodeResponseData> nodes) {
        this.nodes = nodes;
    }

}
