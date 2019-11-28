package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenMintingData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.http.Request;
import io.coti.financialserver.data.MintingFeeQuoteData;
import io.coti.financialserver.http.data.MintingFeeData;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
public class MintingTokenFeeRequest extends Request {

    @NotNull
    private @Valid MintingFeeData mintingFeeData;
    @NotNull
    private @Valid Hash userHash;
    @NotNull
    private @Valid TokenMintingData tokenMintingData;
    private @Valid MintingFeeQuoteData mintingFeeQuoteData;
}
