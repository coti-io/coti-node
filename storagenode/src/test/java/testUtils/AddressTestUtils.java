package testUtils;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.GetHistoryAddressesRequest;

import java.util.*;

public class AddressTestUtils {

    public static AddressData generateRandomAddressData() {
        return new AddressData(HashTestUtils.generateRandomAddressHash());
    }

    public static List<AddressData> generateListOfRandomAddressData(int listSize) {

        List<AddressData> addresses = new ArrayList<>();
        for (int i = 0; i < listSize; i++) {
            addresses.add(generateRandomAddressData());
        }
        return addresses;
    }

    public static Set<AddressData> generateSetOfRandomAddressData(int listSize) {
        Set<AddressData> addresses = new HashSet<>();
        for (int i = 0; i < listSize; i++) {
            addresses.add(generateRandomAddressData());
        }
        return addresses;
    }


    public static GetHistoryAddressesRequest generateGetHistoryAddressesRequest(Hash signerHash, SignatureData signature, AddressData... addresses) {
        List<Hash> addressHashesList = new ArrayList<>();
        Arrays.stream(addresses).forEach(addressData -> addressHashesList.add(addressData.getHash()));

        GetHistoryAddressesRequest getHistoryAddressesRequest = new GetHistoryAddressesRequest();
        getHistoryAddressesRequest.setAddressHashes(addressHashesList);

        if (signerHash != null) {
            getHistoryAddressesRequest.setSignerHash(signerHash);
        }
        if (signature != null) {
            getHistoryAddressesRequest.setSignature(signature);
        }

        return getHistoryAddressesRequest;
    }
}
