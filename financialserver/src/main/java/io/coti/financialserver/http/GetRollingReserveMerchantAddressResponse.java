package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class GetRollingReserveMerchantAddressResponse extends BaseResponse {

    private String addressHash;

    public GetRollingReserveMerchantAddressResponse(Hash addressHash) {
        super();
        this.addressHash = addressHash.toString();
    }
}
