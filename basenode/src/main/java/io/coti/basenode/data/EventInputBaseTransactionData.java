package io.coti.basenode.data;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventInputBaseTransactionData extends InputBaseTransactionData {

    private static final long serialVersionUID = 113649021015050307L;
    @NotNull
    protected @Valid String event;
    @NotNull
    protected @Valid boolean hardFork;

    protected EventInputBaseTransactionData() {
        super();
    }

    public EventInputBaseTransactionData(Hash addressHash, Hash currencyHash, BigDecimal amount, Instant createTime,
                                         String event, boolean hardFork) {
        super(addressHash, currencyHash, amount, createTime);
        this.event = event;
        this.hardFork = hardFork;
    }

    @Override
    public SignatureData getSignatureData() {
        return this.signatureData;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signatureData = signature;
    }
}
