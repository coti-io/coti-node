package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;

public class SignatureData implements IEntity {

    private String r;
    private String s;


    public SignatureData(){

    }

    public SignatureData(String r, String s)
    {
        this.r = r;
        this.s = s;

    }

    public String getR(){ return this.r;}
    public String getS(){ return this.s;}


    @Override
    public Hash getKey() {
        return null;
    }

    @Override
    public void setKey(Hash hash) {

    }
}
