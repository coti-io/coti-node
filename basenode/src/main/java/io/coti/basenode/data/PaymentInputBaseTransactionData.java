package io.coti.basenode.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PaymentInputBaseTransactionData extends InputBaseTransactionData {

    private static final long serialVersionUID = 5056919546430085702L;
    @NotNull
    private List<@Valid PaymentItemData> items;
    @NotNull
    private String encryptedMerchantName;

    private PaymentInputBaseTransactionData() {
        super();
    }
}
