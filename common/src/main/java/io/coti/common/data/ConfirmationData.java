package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ConfirmationData implements IEntity {
    private transient Hash key;
    private Map<Hash, BigDecimal> addressHashToValueTransferredMapping;

    private Date creationTIme;

    private boolean DoubleSpendPreventionConsensus;
    private boolean TrustChainConsensus;

    private TransactionData transactionData;

    public  ConfirmationData(){}

    public ConfirmationData(TransactionData transactionData) {
        addressHashToValueTransferredMapping = new LinkedHashMap<>();
        this.key = transactionData.getHash();
        if (transactionData.isZeroSpend()) {
            return;
        }
        for (BaseTransactionData baseTransactionData :
                transactionData.getBaseTransactions()) {
            addressHashToValueTransferredMapping.put(baseTransactionData.getAddressHash(), baseTransactionData.getAmount());
        }
        this.transactionData = transactionData;
    }

    @Override
    public Hash getKey() {
        return key;
    }

    @Override
    public void setKey(Hash hash) {
        this.key = hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ConfirmationData)) {
            return false;
        }
        return key.equals(((ConfirmationData) other).key);
    }
}
