package io.coti.trustscore.http;

import io.coti.basenode.data.NetworkFeeData;
import io.coti.basenode.data.RollingReserveData;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RollingReserveValidateRequest {
    @NotNull
    private NetworkFeeData networkFeeData;
    @NotNull
    private RollingReserveData rollingReserveData;
    @NotNull
    private String merchantHash;
}
