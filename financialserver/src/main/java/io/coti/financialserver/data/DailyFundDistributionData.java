package io.coti.financialserver.data;


import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;

@Data
public class DailyFundDistributionData implements IEntity {

    private Hash hash;
    @NotEmpty
    private Instant date;
    private LinkedHashMap<Hash, FundDistributionData> fundDistributionEntries;

    public DailyFundDistributionData(Instant date, LinkedHashMap<Hash, FundDistributionData> fundDistributionEntries) {
        this.date = date;
        this.fundDistributionEntries = fundDistributionEntries;
        initHashByDate();
    }

    private void initHashByDate() {
        LocalDateTime ldt = LocalDateTime.ofInstant(date, ZoneOffset.UTC);
        this.hash = CryptoHelper.cryptoHash( (ldt.getYear() + ldt.getMonth().toString() +
                ldt.getDayOfMonth()).getBytes() );
    }
}
