package io.coti.nodemanager.data;

import io.coti.basenode.data.NetworkNodeData;
import lombok.Data;

import java.util.List;

@Data
public class NetworkDetailsForWallet {

    private List<NetworkNodeData> dsps;
    private List<NetworkNodeData> trustScores;



}
