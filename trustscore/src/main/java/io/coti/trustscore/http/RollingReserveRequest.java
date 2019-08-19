package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkFeeData;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RollingReserveRequest {

    @NotNull
    private Hash merchantHash;
    private Hash currencyHash;
    @NotNull
    private NetworkFeeData networkFeeData;

}
