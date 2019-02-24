package io.coti.basenode.http;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Hashtable;
import java.util.Map;

@Data
public class AddEntitiesBulkRequest {
    @NotEmpty(message = "Entities must not be empty")
    private Map<Hash, String> hashToEntityJsonDataMap;

    @NotEmpty(message = "History nodes consensus must not be empty")
    private HistoryNodeConsensusResult historyNodeConsensusResult;

    public AddEntitiesBulkRequest(@NotEmpty(message = "Entities must not be empty") Map<Hash, String> hashToEntityJsonDataMap, @NotEmpty(message = "History nodes consensus must not be empty") HistoryNodeConsensusResult historyNodeConsensusResult) {
        this.hashToEntityJsonDataMap = hashToEntityJsonDataMap;
        this.historyNodeConsensusResult = historyNodeConsensusResult;
    }

    // TODO consider removing this
    public AddEntitiesBulkRequest(@NotEmpty(message = "History nodes consensus must not be empty") HistoryNodeConsensusResult historyNodeConsensusResult) {
        hashToEntityJsonDataMap = new Hashtable<>();
        this.historyNodeConsensusResult = historyNodeConsensusResult;
    }
}

