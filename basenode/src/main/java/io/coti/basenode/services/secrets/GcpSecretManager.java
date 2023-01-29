package io.coti.basenode.services.secrets;

import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.BaseNodeSecretManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Primary
@Service
@ConditionalOnProperty(value = "secret.gcp",
        havingValue = "true")
@Slf4j
public class GcpSecretManager extends BaseNodeSecretManagerService {
    @PostConstruct
    private void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public String getSecret(String secretName) {
        AccessSecretVersionResponse secretValueResponse = getGcpSecretValue(secretName);
        return super.decrypt(secretValueResponse.getPayload().getData().toStringUtf8());
    }

    private AccessSecretVersionResponse getGcpSecretValue(String secretName) {

        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            return client.accessSecretVersion(secretName);
        } catch (IOException e) {
            throw new CotiRunTimeException(e.toString());
        }
    }
}
