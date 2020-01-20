package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class AddNodeEventAdminRequest extends Request {
    @NotNull
    private @Valid Hash nodeHash;
    @NotNull
    private String recordDateTimeUTC;
    @NotNull
    private String nodeType;
    @NotNull
    private String nodeStatus;

    public AddNodeEventAdminRequest(){}

    public AddNodeEventAdminRequest(Hash nodeHash, String recordDateTimeUTC, String nodeType, String nodeStatus) {
        this.nodeHash = nodeHash;
        this.recordDateTimeUTC = recordDateTimeUTC;
        this.nodeType = nodeType;
        this.nodeStatus = nodeStatus;
    }
}
