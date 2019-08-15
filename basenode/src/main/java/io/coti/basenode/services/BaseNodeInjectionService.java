package io.coti.basenode.services;

import io.coti.basenode.data.NetworkType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BaseNodeInjectionService {

    @Autowired
    private static BaseNodeInjector injector;
    public static final NetworkType NETWORK_TYPE = injector.NETWORK_TYPE;
    public static final String AWS_REGION = injector.AWS_REGION;
    public static final String CLUSTER_STAMP_BUCKET_NAME = injector.CLUSTER_STAMP_BUCKET_NAME;

}


