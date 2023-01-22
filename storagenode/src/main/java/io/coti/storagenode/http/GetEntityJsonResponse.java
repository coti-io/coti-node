package io.coti.storagenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.BaseResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.validation.constraints.NotEmpty;

@Data
@EqualsAndHashCode(callSuper = true)
public class GetEntityJsonResponse extends BaseResponse {

    @NotEmpty
    private MutablePair<Hash, String> entityJsonPair;

    public GetEntityJsonResponse(Hash entityHash, String entityJson) {
        this.entityJsonPair = new MutablePair<>(entityHash, entityJson);
    }
}
