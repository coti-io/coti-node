package io.coti.basenode.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ClusterStampPreparationData extends ClusterStampStateData {

    public ClusterStampPreparationData() {
    }

    public ClusterStampPreparationData(long lastDspConfirmed) {
        this.clusterStampHash = new Hash(lastDspConfirmed);
        this.lastDspConfirmed = lastDspConfirmed;
    }
}