package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RollingReserveMerchantAddressResponse extends BaseResponse {

    @NotNull
    String merchantRollingReserveAddress;

    //For Serialization Reasons
    public RollingReserveMerchantAddressResponse(){
    }


    public RollingReserveMerchantAddressResponse(Hash merchantRollingReserveAddress){
        this.merchantRollingReserveAddress = merchantRollingReserveAddress.toHexString();
    }

}
