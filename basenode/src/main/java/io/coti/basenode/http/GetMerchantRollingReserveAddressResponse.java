package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.MerchantRollingReserveAddressData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetMerchantRollingReserveAddressResponse extends BaseResponse {

    @NotNull
    @Valid
    private MerchantRollingReserveAddressData merchantRollingReserveAddressData;

    private GetMerchantRollingReserveAddressResponse() {
    }

    public GetMerchantRollingReserveAddressResponse(Hash merchantHash, Hash merchantRollingReserveAddress) {
        this.merchantRollingReserveAddressData = new MerchantRollingReserveAddressData(merchantHash, merchantRollingReserveAddress);
    }

}
