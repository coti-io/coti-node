package io.coti.basenode.services.secrets;

import io.coti.basenode.services.BaseNodeSecretManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import javax.annotation.PostConstruct;

@Primary
@Service
@ConditionalOnProperty(value = "secret.aws",
        havingValue = "true")
@Slf4j
public class AwsSecretManager extends BaseNodeSecretManagerService {
    @Value("${secret.aws.region.name}")
    private String secretRegionName;

    @PostConstruct
    private void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public String getSecret(String secretName) {
        GetSecretValueResponse secretValueResponse = getAwsSecretValue(secretName);
        return super.decrypt(secretValueResponse.secretString());
    }

    private GetSecretValueResponse getAwsSecretValue(String secretName) {
        try {
            Region region = Region.of(secretRegionName);
            SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                    .region(region)
                    .credentialsProvider(ProfileCredentialsProvider.create())
                    .build();

            GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            GetSecretValueResponse valueResponse = secretsClient.getSecretValue(valueRequest);
            secretsClient.close();
            return valueResponse;
        } catch (SecretsManagerException e) {
            log.error(e.awsErrorDetails().errorMessage());
            throw e;
        }
    }

}
