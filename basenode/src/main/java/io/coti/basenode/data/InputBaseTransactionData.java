package io.coti.basenode.data;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NegativeOrZero;
import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
public class InputBaseTransactionData extends BaseTransactionData {
    @NegativeOrZero
    private BigDecimal amount;

    public InputBaseTransactionData(Hash addressHash, BigDecimal amount, String type, Date createTime) {
        super(addressHash, amount, type, createTime);
    }

}
