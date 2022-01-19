package io.coti.financialserver.http;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class GetNonLockedDistributionEntryRequest {

    @NotEmpty
    private List<@Valid String> excludedState;
}
