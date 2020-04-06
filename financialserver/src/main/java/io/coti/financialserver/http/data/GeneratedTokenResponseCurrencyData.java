package io.coti.financialserver.http.data;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.SignatureData;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class GeneratedTokenResponseCurrencyData {

    protected String currencyName;
    protected String currencySymbol;
    private String currencyHash;
    private String description;
    protected BigDecimal totalSupply;
    protected int scale;
    protected String originatorHash;
    protected SignatureData originatorSignature;
    protected CurrencyType currencyType;
    protected Instant creationTime;
    private String registrarHash;
    protected SignatureData registrarSignature;
    protected BigDecimal mintedAmount;
    protected BigDecimal requestedMintingAmount;

    public GeneratedTokenResponseCurrencyData(CurrencyData token) {
        this.currencyName = token.getName();
        this.currencySymbol = token.getSymbol();
        this.currencyHash = token.getHash().toString();
        this.description = token.getDescription();
        this.totalSupply = token.getTotalSupply();
        this.scale = token.getScale();
        this.originatorHash = token.getOriginatorHash().toString();
        this.originatorSignature = token.getOriginatorSignature();
        this.currencyType = token.getCurrencyTypeData().getCurrencyType();
        this.creationTime = token.getCreateTime();
        this.mintedAmount = BigDecimal.ZERO;
        this.requestedMintingAmount = BigDecimal.ZERO;
    }
}
