package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.AddFundDistributionsResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddFundDistributionsResponse extends BaseResponse {

    private AddFundDistributionsResponseData fundDistributionResponseData;

    public AddFundDistributionsResponse(AddFundDistributionsResponseData addFundDistributionsResponseData) {
        this.fundDistributionResponseData = addFundDistributionsResponseData;
    }
}
