package io.coti.basenode.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class ConstantTokenFeeData extends TokenFeeData {

    private BigDecimal constant;

    public ConstantTokenFeeData() {
        super();
    }

    public ConstantTokenFeeData(String symbol, NodeFeeType nodeFeeType, BigDecimal constant) {
        super(symbol, nodeFeeType);
        this.constant = constant;
    }

    @Override
    public String toString() {
        return String.format("values for fee: %s , constant: %s", getNodeFeeType().name(), constant);
    }

    public BigDecimal getFeeAmount(BigDecimal amount) {
        return this.constant;
    }

    public boolean valid() {
        return constant != null && constant.doubleValue() >= 0;
    }

}
