package io.coti.basenode.http.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IPropagatable;
import lombok.Data;

@Data
public class GetHashToPropagatable<T extends IPropagatable> implements IPropagatable {

    private Hash hash;
    private T data;

    public GetHashToPropagatable() {
    }

    public GetHashToPropagatable(Hash hash, T data) {
        this.hash = hash;
        this.data = data;
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public T getData() {
        return data;
    }

}
