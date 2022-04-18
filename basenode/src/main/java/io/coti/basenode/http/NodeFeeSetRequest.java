package io.coti.basenode.http;

import io.coti.basenode.data.NodeFeeType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class NodeFeeSetRequest {

    NodeFeeType nodeFeeType;
    @NotNull
    @Positive
    BigDecimal feePercentage;
    @NotNull
    @Positive
    BigDecimal feeMinimum;
    @NotNull
    @Positive
    BigDecimal feeMaximum;

}
