package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.DisputeItemData;
import io.coti.financialserver.data.DisputeUpdateItemData;
import io.coti.financialserver.data.FinancialServerEvent;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class KYCEmailRequest  implements Serializable {

    private Hash disputeHash;
    private Hash userHash;
    private FinancialServerEvent financialServerEvent;
    private List<DisputeItemData> disputeItemsData;

    public KYCEmailRequest(Hash disputeHash, Hash userHash, FinancialServerEvent financialServerEvent, List<DisputeItemData> disputeItemsData) {
        this.disputeHash = disputeHash;
        this.userHash = userHash;
        this.financialServerEvent = financialServerEvent;
        this.disputeItemsData = disputeItemsData;
    }
}
