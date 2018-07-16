package io.coti.common.data;

import io.coti.common.data.Hash;
import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

@Data
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


    @Override
    public Hash getKey() {
        return null;
    }

    @Override
    public void setKey(Hash hash) {

    }
}
