package io.coti.cotinode.model;

import io.coti.cotinode.data.AddressData;

public class Addresses extends Collection<AddressData> {

    public Addresses() {
        init();
        dataObjectClass = AddressData.class;
    }

    public void init() {
        super.init();
    }
}
