package io.coti.cotinode.storage.Interfaces;
import io.coti.cotinode.model.Interfaces.IEntity;
import io.coti.cotinode.model.Transaction;

import java.util.List;

public interface IPersistenceProvider {
    void init();
    boolean put(IEntity IEntity);
    List<Transaction> getAllTransactions();
    Transaction getTransaction(byte[] bytes);

    void shutdown();
    void deleteDatabaseFolder();
}
