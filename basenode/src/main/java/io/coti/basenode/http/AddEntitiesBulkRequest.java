package io.coti.basenode.http;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Hashtable;
import java.util.Map;

@Data
public class AddEntitiesBulkRequest extends Request{
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

    // TODO consider removing this, adding it to check deserialization issues
    public AddEntitiesBulkRequest()
    {
        hashToEntityJsonDataMap = new Hashtable<>();
        this.historyNodeConsensusResult = new HistoryNodeConsensusResult();
    }
}

