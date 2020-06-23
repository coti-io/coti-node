package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import javafx.util.Pair;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetEntityJsonResponse extends BaseResponse {

    @NotEmpty
    private Pair<Hash, String> entityJsonPair;

    public GetEntityJsonResponse(Hash entityHash, String entityJson) {
        this.entityJsonPair = new Pair<>(entityHash, entityJson);
    }
}
