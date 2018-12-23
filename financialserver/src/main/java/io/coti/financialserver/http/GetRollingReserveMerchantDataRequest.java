package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.RollingReserveData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetRollingReserveMerchantDataRequest extends Request {

    @NotNull
    private @Valid RollingReserveData rollingReserveData;
}
