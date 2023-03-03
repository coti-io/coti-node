package io.coti.basenode.data;

import io.coti.basenode.exceptions.CotiRunTimeException;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.coti.basenode.services.BaseNodeTransactionHelper.CURRENCY_SCALE;

@EqualsAndHashCode(callSuper = true)
@Data
public class RatioTokenFeeData extends TokenFeeData {

    private FeeData feeData;

    public RatioTokenFeeData(String symbol, NodeFeeType nodeFeeType, FeeData feeData) {
        super(symbol, nodeFeeType);
        this.feeData = feeData;
    }

    @Override
    public String toString() {
        return String.format("values for fee: %s , percent: %s, max: %s, min: %s", getNodeFeeType().name(), feeData.getFeePercentage(), feeData.getMaximumFee(), feeData.getMinimumFee());
    }

    public BigDecimal getFeeAmount(BigDecimal amount) {

        if (!valid()) {
            throw new CotiRunTimeException("Ratio Fee is not Valid!");
        }

        BigDecimal feeAmount;
        BigDecimal fee = amount.multiply(this.feeData.getFeePercentage()).divide(new BigDecimal(100));
        if (fee.compareTo(this.feeData.getMinimumFee()) <= 0) {
            feeAmount = this.feeData.getMinimumFee();
        } else if (fee.compareTo(this.feeData.getMaximumFee()) >= 0) {
            feeAmount = this.feeData.getMaximumFee();
        } else {
            feeAmount = fee;
        }
        if (feeAmount.scale() > CURRENCY_SCALE) {
            feeAmount = feeAmount.setScale(CURRENCY_SCALE, RoundingMode.DOWN);
        }
        if (feeAmount.scale() > 0) {
            feeAmount = feeAmount.stripTrailingZeros();
        }
        return feeAmount;
    }

    public boolean valid() {
        return feeData.getFeePercentage().compareTo(BigDecimal.ZERO) > 0 &&
                feeData.getFeePercentage().compareTo(BigDecimal.valueOf(100)) <= 0 &&
                feeData.getMinimumFee().compareTo(BigDecimal.ZERO) > 0 &&
                feeData.getMinimumFee().compareTo(feeData.getMaximumFee()) <= 0;
    }
}
