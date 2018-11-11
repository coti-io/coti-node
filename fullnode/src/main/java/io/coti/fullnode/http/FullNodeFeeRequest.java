package io.coti.fullnode.http;

import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class FullNodeFeeRequest extends Request {
    @Positive
    public BigDecimal originalAmount;
}
