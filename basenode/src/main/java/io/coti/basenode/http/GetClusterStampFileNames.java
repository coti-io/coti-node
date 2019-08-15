package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
public class GetClusterStampFileNames implements Serializable, ISignable, ISignValidatable {

    private String major;
    @NotNull
    private Set<String> tokens;
    @NotNull
    private Hash signerHash;
    @NotNull
    private SignatureData signature;

    public GetClusterStampFileNames(){
        tokens = new HashSet<>();
    }

    public GetClusterStampFileNames(String major, Set<String> tokens){
        this.major = major;
        this.tokens = tokens;
    }

}
