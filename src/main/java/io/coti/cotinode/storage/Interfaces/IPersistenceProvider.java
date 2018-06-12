package io.coti.cotinode.storage.Interfaces;
import io.coti.cotinode.model.Interfaces.IEntity;
import io.coti.cotinode.model.Interfaces.ITransaction;
import io.coti.cotinode.model.Transaction;

import java.util.List;

public interface IPersistenceProvider {
    void init();
    boolean put(IEntity IEntity);
    List<ITransaction> getAllTransactions();
    ITransaction getTransaction(byte[] key);
    void deleteTransaction(byte[] key);

    void shutdown();
    void deleteDatabaseFolder();
}
