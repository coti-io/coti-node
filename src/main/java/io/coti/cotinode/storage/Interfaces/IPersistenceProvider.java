package io.coti.cotinode.storage.Interfaces;
import io.coti.cotinode.model.Interfaces.IAddress;
import io.coti.cotinode.model.Interfaces.IBaseTransaction;
import io.coti.cotinode.model.Interfaces.IEntity;
import io.coti.cotinode.model.Interfaces.ITransaction;
import io.coti.cotinode.model.Transaction;

import java.util.List;

public interface IPersistenceProvider {
    void init();
    boolean put(IEntity IEntity);
    List<ITransaction> getAllTransactions();
    ITransaction getTransaction(byte[] key);
    IBaseTransaction getBaseTransaction(byte[] key);
    IAddress getAddress(byte[] key);
    void deleteTransaction(byte[] key);
    void deleteBaseTransaction(byte[] key);
    void deleteDatabaseFolder();
    void shutdown();
}
