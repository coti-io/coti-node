package io.coti.basenode.services;

import io.coti.basenode.data.NetworkType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BaseNodeInjector {

    public final NetworkType NETWORK_TYPE;
    public final String AWS_REGION;
    public final String CLUSTER_STAMP_BUCKET_NAME;

    private BaseNodeInjector(@Value("${network}") final NetworkType NETWORK_TYPE,
                             @Value("${aws.s3.bucket.region}") final String AWS_REGION,
                             @Value("${aws.s3.bucket.name.clusterstamp}") final String CLUSTER_STAMP_BUCKET_NAME) {
        this.NETWORK_TYPE = NETWORK_TYPE;
        this.AWS_REGION = AWS_REGION;
        this.CLUSTER_STAMP_BUCKET_NAME = CLUSTER_STAMP_BUCKET_NAME;
    }
}
