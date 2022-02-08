package io.coti.basenode.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventInputBaseTransactionData extends InputBaseTransactionData {

    private static final long serialVersionUID = 113649021015050307L;
    private Event event;

    protected EventInputBaseTransactionData() {
        super();
    }

    public EventInputBaseTransactionData(Hash addressHash, Hash currencyHash, BigDecimal amount, Instant createTime,
                                         Event event) {
        super(addressHash, currencyHash, amount, createTime);
        this.event = event;
    }
}
