package io.coti.basenode.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class ZeroSpendReadyForClusterStampData extends ClusterStampStateData {

    private List<DspReadyForClusterStampData> dspReadyForClusterStampDataList;

    private ZeroSpendReadyForClusterStampData() {
    }

    public ZeroSpendReadyForClusterStampData(long totalConfirmedTransactions) {
        this.clusterStampHash = new Hash(totalConfirmedTransactions);
        this.totalConfirmedTransactionsCount = totalConfirmedTransactions;
    }
}