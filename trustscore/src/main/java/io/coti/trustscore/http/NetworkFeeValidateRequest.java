package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkFeeData;
import io.coti.trustscore.http.data.NetworkFeeResponseData;
import lombok.Data;

@Data
public class NetworkFeeValidateRequest {

    NetworkFeeResponseData rollingReserveResponseData;


    public NetworkFeeData getNetworkFeeData(){
        return new NetworkFeeData(new Hash(rollingReserveResponseData.getAddressHash()), rollingReserveResponseData.getAmount(), rollingReserveResponseData.getOriginalAmount(),
                new Hash(rollingReserveResponseData.getHash()),rollingReserveResponseData.getSignatureData(),rollingReserveResponseData.getCreateTime());
    }
}
