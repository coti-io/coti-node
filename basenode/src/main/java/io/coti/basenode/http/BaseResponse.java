package io.coti.basenode.http;

import io.coti.basenode.http.interfaces.IResponse;
import lombok.Data;

@Data
public abstract class BaseResponse implements IResponse {

    protected String status;

    protected BaseResponse() {
        this.status = BaseNodeHttpStringConstants.STATUS_SUCCESS;
    }

    protected BaseResponse(String status) {
        this.status = status;
    }
}