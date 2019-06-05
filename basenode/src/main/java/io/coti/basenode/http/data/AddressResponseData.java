package io.coti.basenode.http.data;

import io.coti.basenode.data.AddressData;
import lombok.Data;

import java.time.Instant;

@Data
public class AddressResponseData {

    private String hash;
    private Instant creationTime;

    private AddressResponseData() {
    }

    public AddressResponseData(AddressData address) {
        this.hash = address.getHash().toString();
        this.creationTime = address.getCreationTime();
    }
}
