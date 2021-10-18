package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NewCommentResponse extends BaseResponse {

    private String commentHash;

    public NewCommentResponse(Hash commentHash) {
        this.commentHash = commentHash.toString();
    }
}
