package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class GetNodeStatisticsRequest implements IRequest {

    @NotNull
    private @Valid Hash nodeHash;
    @NotNull
    private @Valid LocalDate startDate;
    @NotNull
    private @Valid LocalDate endDate;

}
