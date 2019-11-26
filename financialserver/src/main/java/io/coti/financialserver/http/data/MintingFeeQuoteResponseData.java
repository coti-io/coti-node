package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.data.MintingFeeWarrantData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MintingFeeQuoteResponseData implements IResponse {

    @NotNull
    private String hash;
    @NotNull
    private Instant requestTime;
    @Positive
    protected BigDecimal amount;
    @Positive
    protected String currencyHash;
    @Positive
    protected BigDecimal feeForMinting;
    @NotNull
    private @Valid String signerHash;
    @NotNull
    private @Valid SignatureData signatureData;

    public MintingFeeQuoteResponseData() {
    }

    public MintingFeeQuoteResponseData(MintingFeeWarrantData mintingFeeWarrantData) {
        this.hash = mintingFeeWarrantData.getHash().toString();
        this.requestTime = mintingFeeWarrantData.getRequestTime();
        this.currencyHash = mintingFeeWarrantData.getCurrencyHash().toString();
        this.amount = mintingFeeWarrantData.getAmount();
        this.feeForMinting = mintingFeeWarrantData.getFeeForMinting();
        this.signerHash = mintingFeeWarrantData.getSignerHash().toString();
        this.signatureData = mintingFeeWarrantData.getSignatureData();

    }
}
