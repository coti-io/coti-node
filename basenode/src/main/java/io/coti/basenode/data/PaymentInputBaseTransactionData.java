package io.coti.basenode.data;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PaymentInputBaseTransactionData extends InputBaseTransactionData {
    @NotNull
    private List<@Valid PaymentItemData> items;
    @NotNull
    private String encryptedMerchantName;

    private PaymentInputBaseTransactionData() {
        super();
    }
}
