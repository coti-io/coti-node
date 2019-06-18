package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.HistoryNodeConsensusResult;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AddEntityRequest extends Request {
    @NotEmpty(message = "Hash must not be empty")
    private Hash hash;

    @NotEmpty(message = "Address object must not be empty")
    private String entityJson;

    @NotEmpty(message = "History nodes consensus must not be empty")
    private HistoryNodeConsensusResult historyNodeConsensusResult;

    public AddEntityRequest(@NotEmpty(message = "Hash must not be empty") Hash hash, @NotEmpty(message = "Address object must not be empty") String entityJson, @NotEmpty(message = "History nodes consensus must not be empty") HistoryNodeConsensusResult historyNodeConsensusResult) {
        this.hash = hash;
        this.entityJson = entityJson;
        this.historyNodeConsensusResult = historyNodeConsensusResult;
    }
}
