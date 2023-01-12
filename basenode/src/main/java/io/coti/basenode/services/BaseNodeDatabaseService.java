package io.coti.basenode.services;

import io.coti.basenode.services.interfaces.IDatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static io.coti.basenode.services.BaseNodeServiceManager.databaseConnector;

@Slf4j
@Service
public class BaseNodeDatabaseService implements IDatabaseService {

    @Override
    public boolean compactRange() {
        return databaseConnector.compactRange();
    }
}
