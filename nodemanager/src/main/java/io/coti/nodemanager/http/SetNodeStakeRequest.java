package io.coti.nodemanager.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class SetNodeStakeRequest extends Request implements ISignValidatable {

    @NotNull
    private Hash nodeHash;
    @NotNull
    private BigDecimal stake;
    @NotNull
    private Hash signerHash;
    @NotNull
    private @Valid SignatureData signature;

}
