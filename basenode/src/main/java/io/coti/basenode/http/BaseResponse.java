package io.coti.basenode.http;

import io.coti.basenode.http.interfaces.IResponse;
import lombok.Data;

@Data
public abstract class BaseResponse implements IResponse {
    public String status;

    public BaseResponse() {
        this.status = BaseNodeHttpStringConstants.STATUS_SUCCESS;
    }

    public BaseResponse(String status) {
        this.status = status;
    }
}