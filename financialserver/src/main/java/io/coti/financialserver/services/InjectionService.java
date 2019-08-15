package io.coti.financialserver.services;

import io.coti.basenode.services.BaseNodeInjectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InjectionService extends BaseNodeInjectionService {

    @Autowired
    private static Injector injector;
    public static final String BUCKET_NAME = injector.BUCKET_NAME;
    public static final String BUCKET_NAME_DISTRIBUTION = injector.BUCKET_NAME_DISTRIBUTION;

}
