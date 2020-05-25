package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.interfaces.IRequest;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class SetNodeStakeRequest implements ISignValidatable, IRequest {

    @NotNull
    private @Valid Hash nodeHash;
    @DecimalMin(value = "0")
    private BigDecimal stake;
    @NotNull
    private @Valid Hash signerHash;
    @NotNull
    private @Valid SignatureData signature;

}
