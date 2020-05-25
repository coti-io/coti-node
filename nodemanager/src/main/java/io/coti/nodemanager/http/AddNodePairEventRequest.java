package io.coti.nodemanager.http;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.http.interfaces.IRequest;
import io.coti.nodemanager.data.NetworkNodeStatus;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class AddNodePairEventRequest implements IRequest {

    @NotNull
    private @Valid Hash nodeHash;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private @NotNull @Valid Instant startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private @Valid Instant endTime;
    @NotNull
    private @Valid NodeType nodeType;
    @NotNull
    private @Valid NetworkNodeStatus firstEventNodeStatus;

}
