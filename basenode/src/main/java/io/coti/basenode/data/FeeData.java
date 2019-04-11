package io.coti.basenode.data;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FeeData implements Serializable {
    private BigDecimal feePercentage;
    private BigDecimal minimumFee;
    private BigDecimal maximumFee;

    public FeeData() {
    }

    public FeeData(BigDecimal feePercentage, BigDecimal minimumFee, BigDecimal maximumFee) {
        this.feePercentage = feePercentage;
        this.minimumFee = minimumFee;
        this.maximumFee = maximumFee;
    }
}
