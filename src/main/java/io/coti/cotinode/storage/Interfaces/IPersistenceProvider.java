package io.coti.cotinode.storage.Interfaces;

import io.coti.cotinode.model.Interfaces.*;

import java.util.List;

public interface IPersistenceProvider {
    void init();

    boolean put(IEntity IEntity);

    List<ITransaction> getAllTransactions();

    ITransaction getTransaction(byte[] key);

    IBaseTransaction getBaseTransaction(byte[] key);

    IAddress getAddress(byte[] key);

    IBalance getBalance(byte[] key);

    IPreBalance getPreBalance(byte[] key);

    void deleteTransaction(byte[] key);

    void deleteBaseTransaction(byte[] key);

    void deleteDatabaseFolder();

    void shutdown();
}
