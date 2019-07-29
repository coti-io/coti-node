package io.coti.fullnode.http;

import io.coti.basenode.http.BaseResponse;
import lombok.Data;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class AddressesExistsResponse extends BaseResponse {
    public LinkedHashMap<String, Boolean> addresses;

    public AddressesExistsResponse(LinkedHashMap<String, Boolean> addresses) {
        this.addresses = addresses;
    }

    public AddressesExistsResponse() {
        addresses = new LinkedHashMap<>();
    }


    public void addAddressToResult(String addressHash, Boolean isExists) {
        addresses.putIfAbsent(addressHash, isExists);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddressesExistsResponse)) return false;
        if (!super.equals(o)) return false;
        AddressesExistsResponse that = (AddressesExistsResponse) o;
        Iterator<Map.Entry<String, Boolean>> thisItr = getAddresses().entrySet().iterator();
        Iterator<Map.Entry<String, Boolean>> otherItr = ((AddressesExistsResponse) o).getAddresses().entrySet().iterator();
        while ( thisItr.hasNext() && otherItr.hasNext()) {
            Map.Entry<String, Boolean> thisEntry = thisItr.next();
            Map.Entry<String, Boolean> otherEntry = otherItr.next();
            if (! thisEntry.equals(otherEntry))
                return false;
        }
        return !(thisItr.hasNext() || otherItr.hasNext());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getAddresses());
    }
}


