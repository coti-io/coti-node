package io.coti.basenode.http;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

@Data
public class BulkRequest extends Request implements ISignable, ISignValidatable {

    private Hash signerHash;
    private SignatureData signature;

    public BulkRequest() {
    }

}

