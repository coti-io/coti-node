package io.coti.basenode.data;

import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import java.util.List;

@Data
public class NodeTrustScoreData implements IRequest {

    private Hash nodeHash;
    private Double trustScore;
    private List<NodeTrustScoreDataResult> trustScoreDataResults;

}
