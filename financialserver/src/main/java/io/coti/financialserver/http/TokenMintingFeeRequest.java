package io.coti.financialserver.http;

import io.coti.basenode.data.TokenMintingData;
import io.coti.basenode.http.Request;
import io.coti.financialserver.data.MintingFeeQuoteData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenMintingFeeRequest extends Request {

    @NotNull
    private @Valid TokenMintingData tokenMintingData;
    private @Valid MintingFeeQuoteData mintingFeeQuoteData;
}
