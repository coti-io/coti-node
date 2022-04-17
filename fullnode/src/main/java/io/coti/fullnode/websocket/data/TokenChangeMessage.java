package io.coti.fullnode.websocket.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.data.TokenResponseData;
import lombok.Data;

@Data
public class TokenChangeMessage {

    private Hash currencyHash;
    private TokenResponseData tokenResponseData;

    public TokenChangeMessage(Hash currencyHash, TokenResponseData tokenResponseData) {
        this.currencyHash = currencyHash;
        this.tokenResponseData = tokenResponseData;
    }
}
