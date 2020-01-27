package io.coti.nodemanager.http;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
public class AddNodeBeginEventPairAdminRequest extends Request {
    @NotNull
    private @Valid Hash nodeHash;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private @NotNull @Valid Instant startDateTimeUTC;

    public AddNodeBeginEventPairAdminRequest() {
    }

}
