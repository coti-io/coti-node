package io.coti.common.communication;

import io.coti.common.data.Hash;
import lombok.Data;

@Data
public class DspVote {
    public String voterDspId;

    public Hash transactionHash;

    public boolean isValidTransaction;
}
