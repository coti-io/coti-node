package io.coti.cotinode.storage.Interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.IEntity;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface IDatabaseConnector {

    boolean put(String columnFamilyName, IEntity IEntity);

    void shutdown();

    byte[] getByHash(String columnFamilyName, Hash hash);

    void delete(String columnFamilyName, Hash hash);
}
