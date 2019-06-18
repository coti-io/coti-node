package io.coti.basenode.services.liveview.data;

import lombok.Data;

import java.util.List;

@Data
public class GraphData {
    public List<GraphTransactionData> transactions;
}
