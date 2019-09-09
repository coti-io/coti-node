package io.coti.historynode.http;


import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class GetTransactionsByAddressRequest extends Request implements ISignValidatable, ISignable {

    @NotEmpty
    private Hash address;
    @Valid
    private LocalDate startDate;
    @Valid
    private LocalDate endDate;
    @NotNull
    private @Valid Hash userHash;
    @NotNull
    private @Valid SignatureData userSignature;

    @Override
    public SignatureData getSignature() {
        return userSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.userSignature = signature;
    }

    @Override
    public Hash getSignerHash() {
        return userHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.userHash = signerHash;
    }
}

