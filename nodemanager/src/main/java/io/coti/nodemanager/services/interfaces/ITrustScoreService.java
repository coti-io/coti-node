package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.NetworkNodeData;

import java.util.List;

public interface ITrustScoreService {

    Double getTrustScore(NetworkNodeData nodeHash, List<NetworkNodeData> trustScroeNodeList);

    void setTrustScores(List<NetworkNodeData> nodesList, List<NetworkNodeData> trustScoreNodeList);


}
