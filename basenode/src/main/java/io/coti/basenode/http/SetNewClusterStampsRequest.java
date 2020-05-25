package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class SetNewClusterStampsRequest extends Request implements ISignValidatable {

    @NotEmpty
    private String folderPath;
    @NotEmpty
    private String currencyClusterStampFileName;
    @NotEmpty
    private String balanceClusterStampFileName;

    @NotNull
    private @Valid Hash signerHash;
    @NotNull
    private @Valid SignatureData signature;



}
