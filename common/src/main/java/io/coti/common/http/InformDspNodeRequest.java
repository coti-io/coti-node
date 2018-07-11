package io.coti.common.http;

import lombok.Data;

@Data
public class InformDspNodeRequest extends Request {
    private boolean toPropagate;
}
