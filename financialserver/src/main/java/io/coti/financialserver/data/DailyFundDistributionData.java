package io.coti.financialserver.data;


import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Data
public class DailyFundDistributionData implements IEntity {

    private static final long serialVersionUID = 3367282572738469785L;
    private Hash hash;
    @NotEmpty
    private Instant date;
    private Map<Hash, FundDistributionData> fundDistributionEntries;

    public DailyFundDistributionData(Instant date, Map<Hash, FundDistributionData> fundDistributionEntries) {
        this.date = date;
        this.fundDistributionEntries = fundDistributionEntries;
        initHashByDate();
    }

    private void initHashByDate() {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        this.hash = CryptoHelper.cryptoHash((ldt.getYear() + ldt.getMonth().toString() +
                ldt.getDayOfMonth()).getBytes());
    }
}
