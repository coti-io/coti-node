package io.coti.basenode.services.interfaces;

public interface ISecretManagerService {

    String getSecret(String secretValue, String secretKeySecretName, String type);

    String getSecret(String secretName);

    String decrypt(String nodePrivateKey);
}
