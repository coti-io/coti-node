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
//    @NotEmpty(message = "Hashes must not be empty")
    private List<Hash> hashes;

//    @NotEmpty(message = "History nodes consensus must not be blank")
    private HistoryNodeConsensusResult historyNodeConsensusResult;

    // TODO: Temporay change for checking serialization issues
//    public GetEntitiesBulkRequest(@NotEmpty(message = "Hashes must not be empty") List<Hash> hashes, @NotEmpty(message = "History nodes consensus must not be blank") HistoryNodeConsensusResult historyNodeConsensusResult) {
public GetEntitiesBulkRequest(List<Hash> hashes, HistoryNodeConsensusResult historyNodeConsensusResult) {
        this.hashes = hashes;
        this.historyNodeConsensusResult = historyNodeConsensusResult;
    }

}

