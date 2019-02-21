package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.MerchantRollingReserveData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetMerchantRollingReserveDataRequest extends Request {

    @NotNull
    private @Valid MerchantRollingReserveData merchantRollingReserveData;
}
