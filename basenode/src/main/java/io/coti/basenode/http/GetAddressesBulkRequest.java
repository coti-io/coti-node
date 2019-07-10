package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Data
public class GetAddressesBulkRequest extends GetBulkRequest {
    @NotNull(message = "Address hash must not be null")
    private Set<Hash> addressesHash;


    public GetAddressesBulkRequest() {
        this.addressesHash = new HashSet<>();
    }

    public GetAddressesBulkRequest(@NotNull(message = "Address hash must not be null") Set<Hash> addressesHash) {
        this.addressesHash = addressesHash;
    }


}
