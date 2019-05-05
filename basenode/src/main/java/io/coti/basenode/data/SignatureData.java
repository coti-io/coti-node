package io.coti.basenode.data;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Data
public class SignatureData implements Serializable {

    private static final long serialVersionUID = 5320179588140671704L;
    @NotEmpty
    private String r;
    @NotEmpty
    private String s;

    public SignatureData() {
    }

    public SignatureData(String r, String s) {
        this.r = r;
        this.s = s;
    }

}
