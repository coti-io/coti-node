package io.coti.cotinode.storage.Interfaces;

import io.coti.cotinode.model.*;
import io.coti.cotinode.model.Interfaces.*;

import java.util.List;

public interface IPersistenceProvider {
    void init();

    boolean put(IEntity IEntity);

    List<Transaction> getAllTransactions();

    Transaction getTransaction(byte[] key);

    BaseTransaction getBaseTransaction(byte[] key);

    Address getAddress(byte[] key);

    Balance getBalance(byte[] key);

    PreBalance getPreBalance(byte[] key);

    void deleteTransaction(byte[] key);

    void deleteBaseTransaction(byte[] key);

    void deleteDatabaseFolder();

    void shutdown();
}
