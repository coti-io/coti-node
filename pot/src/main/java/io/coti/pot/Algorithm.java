package io.coti.pot;

import fr.cryptohash.Digest;
import io.coti.pot.interfaces.IAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.security.provider.Sun;

import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class Algorithm implements IAlgorithm {

    private final AlgorithmType hashingAlgorithm;
    private Digest cryptoHashAlgorithm;
    private MessageDigest messageDigestAlgorithm;
    private static List<AlgorithmType> bouncyCastleAlgorithms;

    static {

        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new Sun());

        Provider[] providers = Security.getProviders();
        for (Provider provider : providers) {
            Security.addProvider(provider);
        }

        Algorithm.bouncyCastleAlgorithms = Arrays.asList(AlgorithmType.SHA_512,
                AlgorithmType.BLAKE2B_512, AlgorithmType.KECCAK_512, AlgorithmType.SKEIN_512_512, AlgorithmType.WHIRLPOOL);
    }

    public Algorithm(AlgorithmType hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
        try {
            if (bouncyCastleAlgorithms.contains(hashingAlgorithm))
                this.messageDigestAlgorithm = this.getBouncyCastle(hashingAlgorithm);
            else
                this.cryptoHashAlgorithm = this.getCryptoHash(hashingAlgorithm);
        } catch (Exception e) {
            log.error("Error finding algorithm {} in crypto hash, Exception {}", hashingAlgorithm, e);
        }
    }

    @Override
    public byte[] hash(byte[] input) {
        if (bouncyCastleAlgorithms.contains(hashingAlgorithm))
            return messageDigestAlgorithm.digest(input);
        return cryptoHashAlgorithm.digest(input);
    }

    private MessageDigest getBouncyCastle(AlgorithmType hashingAlgorithm) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(hashingAlgorithm.toString(), new BouncyCastleProvider());
    }

    private Digest getCryptoHash(AlgorithmType hashingAlgorithm) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (Digest) Class.forName("fr.cryptohash." + hashingAlgorithm.toString()).getDeclaredConstructor().newInstance();
    }
}
