package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
public class RollingReserveReleaseDateData implements IEntity {

    private static final long serialVersionUID = 5270626776162833891L;
    private Date date;
    private Map<Hash, RollingReserveReleaseStatus> rollingReserveReleaseStatusByMerchant;

    public RollingReserveReleaseDateData(Date date) {
        rollingReserveReleaseStatusByMerchant = new HashMap<>();
        this.setDate(date);
    }

    public void setDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.date = formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            log.error("Set date error", e);
        }
    }

    @Override
    public Hash getHash() {
        return new Hash(date.getTime());
    }

    @Override
    public void setHash(Hash hash) {
        // no implementation
    }
}
