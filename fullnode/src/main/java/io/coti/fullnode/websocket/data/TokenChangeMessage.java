package io.coti.fullnode.websocket.data;

import io.coti.basenode.http.data.TokenResponseData;
import lombok.Data;

@Data
public class TokenChangeMessage {

    private TokenResponseData token;

    public TokenChangeMessage(TokenResponseData token) {
        this.token = token;
    }
}
