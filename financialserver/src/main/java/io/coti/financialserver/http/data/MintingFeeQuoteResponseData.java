package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.financialserver.data.MintingFeeQuoteData;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class MintingFeeQuoteResponseData implements IResponse {

    private String hash;
    private Instant createTime;
    private BigDecimal mintingAmount;
    private String currencyHash;
    private BigDecimal mintingFee;
    private String signerHash;
    private SignatureData signatureData;

    public MintingFeeQuoteResponseData(MintingFeeQuoteData mintingFeeQuoteData) {
        this.hash = mintingFeeQuoteData.getHash().toString();
        this.createTime = mintingFeeQuoteData.getCreateTime();
        this.currencyHash = mintingFeeQuoteData.getCurrencyHash().toString();
        this.mintingAmount = mintingFeeQuoteData.getMintingAmount();
        this.mintingFee = mintingFeeQuoteData.getMintingFee();
        this.signerHash = mintingFeeQuoteData.getSignerHash().toString();
        this.signatureData = mintingFeeQuoteData.getSignatureData();
    }
}
