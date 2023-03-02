package io.coti.basenode.http;

import io.coti.basenode.data.NodeFeeType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class ConstantTokenFeeSetRequest {

    @NotNull
    @NotEmpty
    String tokenSymbol;
    @Valid
    NodeFeeType nodeFeeType;
    @NotNull
    @Positive
    BigDecimal constant;

}
