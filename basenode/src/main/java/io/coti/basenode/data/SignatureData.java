package io.coti.basenode.data;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class SignatureData implements Serializable {
    @NotNull
    private String r;
    @NotNull
    private String s;

    public SignatureData() {
    }

    public SignatureData(String r, String s) {
        this.r = r;
        this.s = s;
    }

}
