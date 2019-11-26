package io.coti.financialserver.http.data;

import io.coti.basenode.data.*;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
    private BigDecimal mintedAmount;
    private BigDecimal requestedMintingAmount;

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
        this.creationTime = token.getCreationTime();
        this.registrarHash = token.getRegistrarHash().toString();
        this.registrarSignature = token.getRegistrarSignature();
        this.mintedAmount = token.getMintedAmount();
        this.requestedMintingAmount = token.getRequestedMintingAmount();
    }
}
