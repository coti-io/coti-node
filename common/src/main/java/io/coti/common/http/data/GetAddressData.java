package io.coti.common.http.data;

import java.io.Serializable;

public class GetAddressData implements Serializable
{
    public String address;
    public Boolean exists;


    public GetAddressData(String address, boolean exists) {
        this.address = address;
        this.exists = exists;
    }
}
