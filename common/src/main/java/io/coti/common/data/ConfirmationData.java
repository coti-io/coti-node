package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class ConfirmationData implements IEntity {
    private transient Hash hash;
    private Map<Hash, BigDecimal> addressHashToValueTransferredMapping;

    private Date creationTIme;

    private boolean DoubleSpendPreventionConsensus;
    private boolean TrustChainConsensus;


    public  ConfirmationData(){}

    public ConfirmationData(TransactionData transactionData) {
        addressHashToValueTransferredMapping = new LinkedHashMap<>();
        this.hash = transactionData.getHash();
        if (transactionData.isZeroSpend()) {
            return;
        }
        for (BaseTransactionData baseTransactionData :
                transactionData.getBaseTransactions()) {
            addressHashToValueTransferredMapping.put(baseTransactionData.getAddressHash(), baseTransactionData.getAmount());
        }
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ConfirmationData)) {
            return false;
        }
        return hash.equals(((ConfirmationData) other).hash);
    }
}
