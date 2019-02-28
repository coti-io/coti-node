package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.MerchantRollingReserveAddressData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetMerchantRollingReserveAddressResponse extends Response {

    @NotNull
    @Valid
    private MerchantRollingReserveAddressData merchantRollingReserveAddressData;

    private GetMerchantRollingReserveAddressResponse() {
    }

    public GetMerchantRollingReserveAddressResponse(String message, String status) {
        super(message, status);
    }

    public GetMerchantRollingReserveAddressResponse(Hash merchantHash, Hash merchantRollingReserveAddress) {
        super();
        this.merchantRollingReserveAddressData = new MerchantRollingReserveAddressData(merchantHash, merchantRollingReserveAddress);
    }

}
