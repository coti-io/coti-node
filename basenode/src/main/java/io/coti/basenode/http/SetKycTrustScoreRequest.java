package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import lombok.Data;

@Data
public class SetKycTrustScoreRequest extends Request {
    public Hash userHash;
    public SignatureData signature;
    public double kycTrustScore;
}