package io.coti.financialserver.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import lombok.Data;

@Data
public class DeleteTokenMintingQuoteResponse implements IResponse {

    private String mintingFeeWarrantHash;

    public DeleteTokenMintingQuoteResponse() {
    }

    public DeleteTokenMintingQuoteResponse(Hash mintingFeeWarrantHash) {
        this.mintingFeeWarrantHash = mintingFeeWarrantHash.toString();
    }
}
