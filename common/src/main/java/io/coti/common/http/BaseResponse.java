package io.coti.common.http;

import io.coti.common.http.interfaces.IResponse;
import lombok.Data;

@Data
public abstract class BaseResponse implements IResponse {
    public String status;



    public BaseResponse() {
        this.status = HttpStringConstants.STATUS_SUCCESS;
    }

    public BaseResponse(String status) {
        this.status = status;
    }
}
