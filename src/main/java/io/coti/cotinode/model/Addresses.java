package io.coti.cotinode.model;

import io.coti.cotinode.data.AddressData;

import javax.annotation.PostConstruct;

public class Addresses extends Collection<AddressData> {

    public Addresses() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
