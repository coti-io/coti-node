package testUtils;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class AddressAsObjectAndJsonString {
    private Hash hash;
    private AddressData addressData;
    private String addressAsJsonString;

    public AddressAsObjectAndJsonString(Hash hash, AddressData addressData, String addressAsJsonString) {
        this.hash = hash;
        this.addressData = addressData;
        this.addressAsJsonString = addressAsJsonString;
    }
}
