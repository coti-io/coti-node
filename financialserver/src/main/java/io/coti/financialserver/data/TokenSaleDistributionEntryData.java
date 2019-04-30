package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

@Data
public class TokenSaleDistributionEntryData implements IEntity {

    private String fundName;    // Should corresponds to the values of TokenSale
    private BigDecimal amount;
    private String identifyingDescription;
    private Hash distributionHash;
    protected boolean completedSuccessfully;

    public TokenSaleDistributionEntryData(String fundName, BigDecimal amount, String identifyingDescription) {
        this.fundName = fundName;
        this.amount = amount;
        this.identifyingDescription = identifyingDescription;
        this.completedSuccessfully = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenSaleDistributionEntryData that = (TokenSaleDistributionEntryData) o;
        return Objects.equals(fundName, that.fundName) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(identifyingDescription, that.identifyingDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fundName, amount, identifyingDescription);
    }

    @Override
    public Hash getHash() {
        return distributionHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.distributionHash = hash;
    }
}
