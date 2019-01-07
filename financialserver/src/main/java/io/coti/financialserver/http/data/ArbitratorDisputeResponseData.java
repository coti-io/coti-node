package io.coti.financialserver.http.data;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.DisputeData;
import io.coti.financialserver.data.DisputeItemData;
import lombok.Data;

import java.util.List;

@Data
public class ArbitratorDisputeResponseData extends GetDisputeResponseData {

    private ArbitratorDisputeResponseData() {
        super();
    }

    public ArbitratorDisputeResponseData(DisputeData disputeData, Hash arbitratorHash) {
        super(disputeData);
        setDisputeItems(disputeData.getDisputeItems(), arbitratorHash);
    }

    private void setDisputeItems(List<DisputeItemData> disputeItems, Hash arbitratorHash) {
        disputeItems.forEach(disputeItemData -> this.disputeItems.add(new ArbitratorDisputeItemResponseData(disputeItemData, arbitratorHash)));
    }
}
