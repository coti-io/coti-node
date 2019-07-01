package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Data
public class UpdateDistributionAmountRequest {

    @NotNull
    private LocalDateTime distributionDate;
    @NotNull
    private @Valid Hash distributionHash;
    @NotNull
    private BigDecimal distributionAmount;

    public void setDistributionDate(String distributionDate) {
        try {
            this.distributionDate = LocalDate.parse(distributionDate).atStartOfDay();
        } catch (DateTimeParseException e) {
            return;
        }
    }
}
