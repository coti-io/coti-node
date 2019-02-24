package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class GetEntitiesBulkRequest extends Request {
    @NotEmpty(message = "Hashes must not be empty")
    private List<Hash> hashes;

    @NotEmpty(message = "History nodes consensus must not be blank")
    private HistoryNodeConsensusResult historyNodeConsensusResult;

    public GetEntitiesBulkRequest(@NotEmpty(message = "Hashes must not be empty") List<Hash> hashes, @NotEmpty(message = "History nodes consensus must not be blank") HistoryNodeConsensusResult historyNodeConsensusResult) {
        this.hashes = hashes;
        this.historyNodeConsensusResult = historyNodeConsensusResult;
    }

}

