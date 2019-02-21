package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class MerchantRollingReserveData implements IEntity {

    private Hash merchantHash;
    private Hash merchantRollingReserveAddress;


    @Override
    public Hash getHash() {
        return merchantHash;
    }

    @Override
    public void setHash(Hash hash) {
        merchantHash = hash;
    }


}
