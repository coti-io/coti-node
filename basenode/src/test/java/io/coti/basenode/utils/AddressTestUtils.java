package io.coti.basenode.utils;

import io.coti.basenode.data.AddressData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}
