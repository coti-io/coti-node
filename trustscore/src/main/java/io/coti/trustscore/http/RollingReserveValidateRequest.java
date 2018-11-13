package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.RollingReserveData;
import io.coti.trustscore.http.data.RollingReserveResponseData;
import lombok.Data;

@Data
public class RollingReserveValidateRequest {

    RollingReserveResponseData rollingReserveResponseData;


    public RollingReserveData getRollingReserveData(){

        RollingReserveData rollingReserveData = new RollingReserveData(new Hash(rollingReserveResponseData.getAddressHash()), rollingReserveResponseData.getAmount(),
                rollingReserveResponseData.getOriginalAmount() ,rollingReserveResponseData.getCreateTime());
        rollingReserveData.setHash(new Hash(rollingReserveResponseData.getHash()));
        return rollingReserveData;
    }
}
