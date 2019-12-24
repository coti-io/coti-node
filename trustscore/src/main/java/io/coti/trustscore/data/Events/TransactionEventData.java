package io.coti.trustscore.data.Events;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.tsenums.TransactionEventType;
import io.coti.trustscore.data.tsenums.UserType;
import io.coti.trustscore.data.UnlinkedAddressData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionEventData extends EventData {

    private static final long serialVersionUID = -88042864576760047L;
    private UserType userType;
    private TransactionEventType transactionEventType;
    private BigDecimal amount;
    private UnlinkedAddressData unlinkedAddressData;

    public TransactionEventData(TransactionData transactionData, UserType userType) {
        this.amount = transactionData.getAmount();
        this.userType = userType;
        this.transactionEventType = TransactionEventType.SENDER_EVENT;
        super.setEventDate(transactionData.getDspConsensusResult().getIndexingTime());
        super.setEventType(EventType.TRANSACTION);
        super.setUniqueIdentifier(transactionData.getHash());
    }

    public TransactionEventData(TransactionData transactionData, BaseTransactionData baseTransactionData, UserType userType) {
        this.amount = baseTransactionData.getAmount();
        this.userType = userType;
        this.transactionEventType = TransactionEventType.RECEIVER_EVENT;
        super.setEventDate(transactionData.getDspConsensusResult().getIndexingTime());
        super.setEventType(EventType.TRANSACTION);
        super.setUniqueIdentifier(baseTransactionData.getHash());
    }

    public void setUnlinkedAddressData(UnlinkedAddressData unlinkedAddressData) {
        this.unlinkedAddressData = unlinkedAddressData;
        this.transactionEventType = TransactionEventType.SENDER_NEW_ADDRESS_EVENT;
    }
}

// todo to delete