package io.coti.financialserver.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Data
public class DistributionReleaseDateData implements IEntity {

    private Date date;
    private List<Hash> distributionHashesList;

    public DistributionReleaseDateData(Date date) {
        distributionHashesList = new ArrayList<>();
        this.setDate(date);
    }

    public void setDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            this.date = formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Hash getHash() {
        return new Hash(date.getTime());
    }

    @Override
    public void setHash(Hash hash) {
        // TODO: implement this
    }
}
