package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class GetRollingReserveMerchantAddressResponse extends BaseResponse {

    private String adressHash;

    public GetRollingReserveMerchantAddressResponse(Hash adressHash) {
        super();
        this.adressHash = adressHash.toString();
    }
}
