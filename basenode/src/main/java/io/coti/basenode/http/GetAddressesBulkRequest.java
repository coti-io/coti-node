package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
public class GetAddressesBulkRequest extends BulkRequest {

    @NotNull(message = "Address hash must not be null")
    private List<Hash> addressesHash;

    public GetAddressesBulkRequest() {
        this.addressesHash = new ArrayList<>();
    }

    public GetAddressesBulkRequest(@NotNull(message = "Address hash must not be null") List<Hash> addressesHash) {
        this.addressesHash = addressesHash;
    }


}
