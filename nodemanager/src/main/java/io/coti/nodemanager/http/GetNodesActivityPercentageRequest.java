package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
public class GetNodesActivityPercentageRequest extends Request {

    @NotNull
    private List<@Valid Hash> nodeHashes;
    @NotNull
    private @Valid LocalDate startDate;
    @NotNull
    private @Valid LocalDate endDate;
}
