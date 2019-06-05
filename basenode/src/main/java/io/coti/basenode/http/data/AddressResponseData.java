package io.coti.basenode.http.data;

import io.coti.basenode.data.AddressData;
import lombok.Data;

import java.time.Instant;

@Data
public class AddressResponseData {

    private String address;
    private Instant creationTime;

    private AddressResponseData() {
    }

    public AddressResponseData(AddressData address) {
        this.address = address.getHash().toString();
        this.creationTime = address.getCreationTime();
    }
}
