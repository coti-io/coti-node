package io.coti.common.http.websocket;

import io.coti.common.data.Hash;
import lombok.Data;

@Data
public class BalanceSubscriptionRequest {
    private Hash addressHash;
}
