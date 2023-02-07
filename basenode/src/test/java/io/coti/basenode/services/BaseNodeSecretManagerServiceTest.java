package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ContextConfiguration(classes = {BaseNodeSecretManagerService.class})

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@ExtendWith(SpringExtension.class)
@Slf4j
class BaseNodeSecretManagerServiceTest {

    @Value("${secret.private.key.file.name}")
    private String privateKeyFileName;
    @Value("${seed}")
    private String seed;
    @Value("${secret.algorithm}")
    private String algorithm;

    @Autowired
    BaseNodeSecretManagerService secretManagerService;

    @Test
    void encryptDecryptTest() throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IOException {
        KeyPair keyPair = generateKeys(algorithm);

        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] privateKey = keyPair.getPrivate().getEncoded();
        try (FileOutputStream fos = new FileOutputStream(privateKeyFileName)) {
            fos.write(privateKey);
        }

        String encryptedSecret = CryptoHelper.encryptString(seed, publicKey, algorithm);
        String decryptedSecret = secretManagerService.decrypt(encryptedSecret);

        assertThat(seed).contains(decryptedSecret);
    }

    private KeyPair generateKeys(String algorithm) throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
        keyGen.initialize(2048);
        return keyGen.genKeyPair();
    }
}
