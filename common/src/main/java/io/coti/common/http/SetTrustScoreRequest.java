package io.coti.common.http;

import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;

public class SetTrustScoreRequest extends Request {
    public Hash userHash;
    public SignatureData signatureData;
    public double trustScore;
}