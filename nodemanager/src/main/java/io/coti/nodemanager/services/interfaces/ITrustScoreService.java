package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.NetworkNodeData;

public interface ITrustScoreService {

    Double getTrustScore(NetworkNodeData nodeHash);

}
