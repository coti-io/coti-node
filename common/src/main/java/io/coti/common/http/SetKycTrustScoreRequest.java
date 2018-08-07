package io.coti.common.http;

import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;

public class SetKycTrustScoreRequest extends Request {
    public Hash userHash;
    public SignatureData signature;
    public double kycTrustScore;
}