package io.coti.financialserver.data;

import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class FinancialServerTransactionData implements IEntity {

    private Hash receiverBaseTransactionAddressHash;

    public FinancialServerTransactionData(Hash receiverBaseTransactionAddressHash) {
        this.receiverBaseTransactionAddressHash = receiverBaseTransactionAddressHash;
    }

    public Hash getHash() {
        return receiverBaseTransactionAddressHash;
    }

    @Override
    public void setHash(Hash hash) {
        receiverBaseTransactionAddressHash = hash;
    }
}
