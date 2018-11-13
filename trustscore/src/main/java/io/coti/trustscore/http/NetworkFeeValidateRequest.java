package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkFeeData;
import io.coti.trustscore.http.data.NetworkFeeResponseData;
import lombok.Data;

@Data
public class NetworkFeeValidateRequest {

    NetworkFeeResponseData rollingReserveResponseData;


    public NetworkFeeData getNetworkFeeData(){
        NetworkFeeData networkFeeData = new NetworkFeeData(new Hash(rollingReserveResponseData.getAddressHash()), rollingReserveResponseData.getAmount(), rollingReserveResponseData.getOriginalAmount(),
               rollingReserveResponseData.getCreateTime());
        networkFeeData.setHash(new Hash(rollingReserveResponseData.getHash()));
        return networkFeeData;
    }
}
