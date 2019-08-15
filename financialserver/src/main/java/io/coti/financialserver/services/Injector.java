package io.coti.financialserver.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class Injector {

    public final String BUCKET_NAME;
    public final String BUCKET_NAME_DISTRIBUTION;

    private Injector(@Value("${aws.s3.bucket.name}") final String BUCKET_NAME, @Value("${aws.s3.bucket.name.distribution}") final String BUCKET_NAME_DISTRIBUTION) {
        this.BUCKET_NAME = BUCKET_NAME;
        this.BUCKET_NAME_DISTRIBUTION = BUCKET_NAME_DISTRIBUTION;
    }
}
