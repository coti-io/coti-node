package io.coti.basenode.data;

import io.coti.basenode.http.Request;
import lombok.Data;

import java.util.List;

@Data
public class NodeTrustScoreData extends Request {
    private Hash nodeHash;
    private Double trustScore;
    private List<NodeTrustScoreDataResult> trustScoreDataResults;
}
