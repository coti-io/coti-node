package io.coti.basenode.data;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class ZeroSpendIsReadyForClusterStampData extends ClusterStampStateData {

    private List<DspReadyForClusterStampData> dspReadyForClusterStampDataList;

    private ZeroSpendIsReadyForClusterStampData() {
    }

    public ZeroSpendIsReadyForClusterStampData(long totalConfirmedTransactions) {
        this.clusterStampHash = new Hash(totalConfirmedTransactions);
        this.totalConfirmedTransactionsCount = totalConfirmedTransactions;
    }
}