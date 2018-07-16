package io.coti.common.services.interfaces;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;

public interface IAddressService {

    boolean addNewAddress(Hash addressHash);

    boolean addressExists(Hash addressHash);

    TransactionData[] addressTransactions(Hash addressHash);
}
