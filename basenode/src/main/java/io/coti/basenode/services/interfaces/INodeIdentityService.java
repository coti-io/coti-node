package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;

public interface INodeIdentityService {

    void init();

    Hash getNodeHash();

    SignatureData signMessage(byte[] signatureMessage);

    SignatureData signMessage(byte[] message, Integer index);

    Hash generateAddress(Integer index);

    Hash generateAddress(String seed, Integer index);

    void setSeed(String seed);
}
