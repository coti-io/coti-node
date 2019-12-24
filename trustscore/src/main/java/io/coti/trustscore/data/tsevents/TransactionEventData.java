package io.coti.trustscore.data.tsevents;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.data.UnlinkedAddressData;
import io.coti.trustscore.data.tsenums.TransactionEventType;
import io.coti.trustscore.data.tsenums.UserType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class TransactionEventData extends EventData {

    private static final long serialVersionUID = 1125233993765735292L;
    private UserType userType;
    private TransactionEventType transactionEventType;
    private BigDecimal amount;
    private UnlinkedAddressData unlinkedAddressData;

    public TransactionEventData(TransactionData transactionData, UserType userType) {
        this.amount = transactionData.getAmount();
        this.userType = userType;
        this.transactionEventType = TransactionEventType.SENDER_EVENT;
        super.setEventDate(LocalDateTime.ofInstant(transactionData.getDspConsensusResult().getIndexingTime(), ZoneOffset.UTC).toLocalDate());
        super.setHash(transactionData.getHash());
    }

    public TransactionEventData(TransactionData transactionData, BaseTransactionData baseTransactionData, UserType userType) {
        this.amount = baseTransactionData.getAmount();
        this.userType = userType;
        this.transactionEventType = TransactionEventType.RECEIVER_EVENT;
        super.setEventDate(LocalDateTime.ofInstant(transactionData.getDspConsensusResult().getIndexingTime(), ZoneOffset.UTC).toLocalDate());
        super.setHash(baseTransactionData.getHash());
    }

    public void setUnlinkedAddressData(UnlinkedAddressData unlinkedAddressData) {
        this.unlinkedAddressData = unlinkedAddressData;
        this.transactionEventType = TransactionEventType.SENDER_NEW_ADDRESS_EVENT;
    }
}
