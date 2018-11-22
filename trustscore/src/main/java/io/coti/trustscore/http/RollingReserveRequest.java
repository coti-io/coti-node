package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkFeeData;
import io.coti.basenode.http.Response;
import lombok.Data;
import org.springframework.http.ResponseEntity;

import javax.validation.constraints.NotNull;

@Data
public class RollingReserveRequest {
    @NotNull
    Hash userHash;
    @NotNull
    NetworkFeeData networkFeeData;

}
