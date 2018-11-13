package io.coti.trustscore.http;

import io.coti.basenode.data.NetworkFeeData;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class NetworkFeeValidateRequest {
    @NotNull
    NetworkFeeData networkFeeData;

}
