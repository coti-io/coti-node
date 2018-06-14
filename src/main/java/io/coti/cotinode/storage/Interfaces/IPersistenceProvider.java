package io.coti.cotinode.storage.Interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.model.*;
import io.coti.cotinode.model.Interfaces.*;

import java.util.List;

public interface IPersistenceProvider {
    void init();

    boolean put(IEntity IEntity);

    List<Transaction> getAllTransactions();

    Transaction getTransaction(Hash key);

    BaseTransaction getBaseTransaction(Hash key);

    Address getAddress(Hash key);

    Balance getBalance(Hash key);

    PreBalance getPreBalance(Hash key);

    void deleteTransaction(Hash key);

    void deleteBaseTransaction(Hash key);

    void deleteDatabaseFolder();

    void shutdown();
}
