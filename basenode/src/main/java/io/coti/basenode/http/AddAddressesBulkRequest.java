package io.coti.basenode.http;

import io.coti.basenode.data.AddressData;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class AddAddressesBulkRequest extends BulkRequest {
    @NotEmpty(message = "Entities must not be empty")
    private List<AddressData> addresses;


    public AddAddressesBulkRequest(@NotEmpty(message = "Entities must not be empty") List<AddressData> addresses) {
        this.addresses = addresses;
    }

    public AddAddressesBulkRequest() {
        addresses = new ArrayList<>();
    }
}

