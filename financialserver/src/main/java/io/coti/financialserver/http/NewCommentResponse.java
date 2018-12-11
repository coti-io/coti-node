package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;

@Data
public class NewCommentResponse extends BaseResponse {

    private String commentHash;

    public NewCommentResponse(Hash commentHash) {
        super();
        this.commentHash = commentHash.toString();
    }
}
