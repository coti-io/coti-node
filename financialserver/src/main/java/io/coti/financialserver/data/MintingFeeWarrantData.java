package io.coti.financialserver.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Data
public class MintingFeeWarrantData implements IEntity, ISignable, ISignValidatable {

    public static transient final int WARRANT_TERM_MINUTES = 60;
    private static final long serialVersionUID = 7595335404562613092L;
    @NotNull
    private Hash hash;
    @NotNull
    private Instant requestTime;
    @Positive
    protected BigDecimal amount;
    @Positive
    protected Hash currencyHash;
    @Positive
    protected BigDecimal feeForMinting;
    @NotNull
    private @Valid Hash signerHash;
    @NotNull
    private @Valid SignatureData signatureData;

    public MintingFeeWarrantData() {
    }

    public MintingFeeWarrantData(Hash currencyHash, Instant requestTime, BigDecimal amount, BigDecimal feeForMinting) {
        this.hash = CryptoHelper.cryptoHash(ArrayUtils.addAll(currencyHash.getBytes(), requestTime.toString().getBytes()));
        this.requestTime = requestTime;
        this.currencyHash = currencyHash;
        this.amount = amount;
        this.feeForMinting = feeForMinting;
    }

    @Override
    public SignatureData getSignature() {
        return this.signatureData;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signatureData = signature;
    }

    public boolean isStillValid(Instant systemTime) {
        return requestTime.isAfter(systemTime.minus(WARRANT_TERM_MINUTES, ChronoUnit.MINUTES)) && requestTime.isBefore(systemTime.plus(1, ChronoUnit.MINUTES));
    }
}
