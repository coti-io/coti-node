package io.coti.zerospend.services;

import io.coti.basenode.exceptions.AwsException;
import io.coti.basenode.services.BaseNodeAwsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Primary
public class AwsService extends BaseNodeAwsService {

    @Override
    public void init() {
        if (!buildS3ClientWithCredentials) {
            throw new AwsException("AWS S3 client with credentials should be set to true");
        }
        super.init();
    }
}
