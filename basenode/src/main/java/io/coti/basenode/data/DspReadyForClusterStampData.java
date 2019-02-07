package io.coti.basenode.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class DspReadyForClusterStampData extends ClusterStampStateData {

    private List<FullNodeReadyForClusterStampData> fullNodeReadyForClusterStampDataList;

    public DspReadyForClusterStampData(Hash hash) {
        this.clusterStampHash = hash;
        this.fullNodeReadyForClusterStampDataList = new ArrayList<>();
    }
}