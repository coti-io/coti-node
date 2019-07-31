package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Data
public class AddHistoryEntitiesResponse extends BaseResponse implements ISignValidatable, ISignable {

    @NotEmpty
    private Map<Hash, Boolean> hashToStoreResultMap;
    private SignatureData signature;
    private Hash signerHash;

    private AddHistoryEntitiesResponse() {
    }

    public AddHistoryEntitiesResponse(Map<Hash, Boolean> addressHashesToStoreResult) {
        this.hashToStoreResultMap = addressHashesToStoreResult;
    }
}
