package io.coti.cotinode.http.websocket;

import io.coti.cotinode.data.Hash;
import lombok.Data;

@Data
public class BalanceSubscriptionRequest {
    private Hash addressHash;
}
