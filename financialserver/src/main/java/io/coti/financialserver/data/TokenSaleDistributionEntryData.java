package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Data
public class TokenSaleDistributionEntryData implements Serializable {
    @NotNull
    private TokenSale fundName;
    @Positive
    private BigDecimal amount;
    @NotNull
    private String identifyingDescription;
    private Hash transactionHash;
    protected boolean completedSuccessfully;

    private TokenSaleDistributionEntryData() {
    }

    public void setFundName(String fundName) {
        this.fundName = TokenSale.getNameByText(fundName);
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

}
