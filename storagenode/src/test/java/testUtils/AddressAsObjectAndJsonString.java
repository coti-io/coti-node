package testUtils;

import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import lombok.Data;

@Data
public class AddressAsObjectAndJsonString {
    private Hash hash;
    private AddressTransactionsHistory addressTransactionsHistory;
    private String addressAsJsonString;

    public AddressAsObjectAndJsonString(Hash hash, AddressTransactionsHistory addressTransactionsHistory, String addressAsJsonString) {
        this.hash = hash;
        this.addressTransactionsHistory = addressTransactionsHistory;
        this.addressAsJsonString = addressAsJsonString;
    }
}
