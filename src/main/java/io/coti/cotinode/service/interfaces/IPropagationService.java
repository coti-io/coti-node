package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;

public interface IPropagationService {
    void getTransactionFromNeighbors(Hash leftParentHash);
}
