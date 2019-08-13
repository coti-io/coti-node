package io.coti.basenode.data;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PaymentItemData implements Serializable {

    private static final long serialVersionUID = 1877040140514962938L;
    @NotNull
    private Long itemId;
    @NotNull
    private BigDecimal itemPrice;
    @NotNull
    private String itemName;
    @NotNull
    private int itemQuantity;

}
