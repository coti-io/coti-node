package io.coti.common.model;

import io.coti.common.data.AddressData;

import javax.annotation.PostConstruct;

public class Addresses extends Collection<AddressData> {

    public Addresses() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
