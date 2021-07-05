package io.coti.basenode.services;

import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.services.interfaces.IDatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseNodeDatabaseService implements IDatabaseService {

    @Autowired
    private IDatabaseConnector dBConnector;

    @Override
    public boolean compactRange() {
        return dBConnector.compactRange();
    }
}
