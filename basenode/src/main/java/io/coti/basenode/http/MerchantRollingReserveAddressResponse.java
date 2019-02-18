package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class MerchantRollingReserveAddressResponse extends BaseResponse {

    @NotNull
    String merchantRollingReserveAddress;

    //For Serialization Reasons
    public MerchantRollingReserveAddressResponse(){
    }


    public MerchantRollingReserveAddressResponse(Hash merchantRollingReserveAddress){
        this.merchantRollingReserveAddress = merchantRollingReserveAddress.toHexString();
    }

}
