package io.coti.trustscore.http;

import io.coti.basenode.data.NetworkFeeData;
import io.coti.basenode.data.RollingReserveData;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RollingReserveValidateRequest {
    @NotNull
    NetworkFeeData networkFeeData;
    @NotNull
    RollingReserveData rollingReserveData;

}
