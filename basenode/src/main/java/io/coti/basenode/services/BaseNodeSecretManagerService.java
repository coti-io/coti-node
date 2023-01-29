package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.interfaces.ISecretManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static io.coti.basenode.services.BaseNodeServiceManager.secretManagerService;
import static java.lang.String.format;

@Service
@Slf4j
public class BaseNodeSecretManagerService implements ISecretManagerService {

    @Value("${secret.private.key.file.name:}")
    private String privateKeyFileName;
    @Value("${secret.algorithm:RSA}")
    private String algorithm;

    public String getSecret(String secretValue, String secretKeySecretName, String type) {
        secretValue = !secretValue.isEmpty() ? decrypt(secretValue) : getSecret(secretKeySecretName);
        if (secretValue.isEmpty()) {
            throw new CotiRunTimeException(format("Critical! Node configuration missing %s value.", type));
        }
        return secretValue;
    }

    public String getSecret(String secretName) {
        throw new UnsupportedOperationException("No valid implementation of secret found!");
    }

    public String decrypt(String encryptedSecret) {

        if (privateKeyFileName.isEmpty()) {
            return encryptedSecret;
        }

        try {
            byte[] privateKey = Files.readAllBytes(Paths.get(privateKeyFileName));
            return CryptoHelper.decryptString(encryptedSecret, privateKey, algorithm);
        } catch (IOException | InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            log.error(e.toString());
            return null;
        }
    }

}
