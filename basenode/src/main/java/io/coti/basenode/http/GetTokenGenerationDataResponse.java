package io.coti.basenode.http;

import io.coti.basenode.data.CurrencyData;
import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

//TODO 9/17/2019 astolia: move to financial server
@Data
public class GetTokenGenerationDataResponse extends BaseResponse {

    @NotNull
    private Map<Hash, CurrencyData> transactionHashToGeneratedCurrency;

}
