package io.coti.basenode.data;

import lombok.Data;

import java.io.Serializable;

@Data
public class SignatureData implements Serializable {

    private String r;
    private String s;

    public SignatureData() {
    }

    public SignatureData(String r, String s) {
        this.r = r;
        this.s = s;
    }

}
