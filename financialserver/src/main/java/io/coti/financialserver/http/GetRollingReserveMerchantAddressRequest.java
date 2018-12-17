package io.coti.financialserver.http;

import io.coti.basenode.http.Request;
import io.coti.financialserver.data.DisputeCommentData;
import io.coti.financialserver.data.RollingReserveAddressData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class GetRollingReserveMerchantAddressRequest extends Request {

    @NotNull
    private @Valid RollingReserveAddressData rollingReserveAddressData;
}
