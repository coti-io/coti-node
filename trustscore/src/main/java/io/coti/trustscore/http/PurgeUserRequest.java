package io.coti.trustscore.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Request;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PurgeUserRequest extends Request {

    private static final long serialVersionUID = -7955142018630882110L;
    @NotNull
    public Hash userHash;
}

