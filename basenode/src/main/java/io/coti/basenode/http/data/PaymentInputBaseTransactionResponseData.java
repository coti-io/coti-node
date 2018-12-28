package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.PaymentInputBaseTransactionData;
import io.coti.basenode.data.PaymentItemData;
import lombok.Data;

import java.util.List;

@Data
public class PaymentInputBaseTransactionResponseData extends InputBaseTransactionResponseData {
    private List<PaymentItemData> items;
    private String encryptedMerchantName;

    public PaymentInputBaseTransactionResponseData(BaseTransactionData baseTransactionData) {
        super(baseTransactionData);
        PaymentInputBaseTransactionData paymentInputBaseTransactionData = (PaymentInputBaseTransactionData) baseTransactionData;
        this.items = paymentInputBaseTransactionData.getItems();
        this.encryptedMerchantName = paymentInputBaseTransactionData.getEncryptedMerchantName();

    }
}
