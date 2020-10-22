package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetBlacklistNodesResponse extends BaseResponse {

    private Set<String> blacklistNodes;

    public GetBlacklistNodesResponse(Set<Hash> blacklistNodes) {
        this.blacklistNodes = blacklistNodes.stream().map(Hash::toString).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
