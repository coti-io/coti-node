package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.financialserver.data.Fund;
import io.coti.financialserver.data.FundDistributionData;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Data
public class GetDistributionsByDateRequest {

    @NotNull
    private LocalDateTime distributionDate;
    private Hash receiverAddress;
    private Fund distributionPoolFund;
    private String source;

    public void setDistributionDate(String distributionDate) {
        try {
            this.distributionDate = LocalDate.parse(distributionDate).atStartOfDay();
        } catch (DateTimeParseException e) {
            return;
        }
    }

    @NotNull
    public Hash getDistributionHash() {
        if ((receiverAddress == null) == (distributionPoolFund == null) == (source == null)) {
            if (receiverAddress == null) {
                return new Hash("");
            }
            FundDistributionData fundDistributionData = new FundDistributionData(receiverAddress, distributionPoolFund, source);
            fundDistributionData.setHash();
            return fundDistributionData.getHash();
        }
        return null;
    }
}
