package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class NewItemResponse extends BaseResponse {

    public NewItemResponse(String status) {
        super(status);
    }
}
