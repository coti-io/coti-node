package io.coti.pot;

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

    private AlgorithmTypes hashingAlgorithm;
    fr.cryptohash.Digest cryptoHashAlgorithm;
    private MessageDigest messageDigestAlgorithm;
    private static List<AlgorithmTypes> bouncyCastleAlgorithms;

    static {

        Security.addProvider(new BouncyCastleProvider());
        Security.addProvider(new Sun());
        Algorithm.bouncyCastleAlgorithms = Arrays.asList(AlgorithmTypes.SHA_512,
                AlgorithmTypes.BLAKE2B_512, AlgorithmTypes.KECCAK_512, AlgorithmTypes.Skein_512_512, AlgorithmTypes.WHIRLPOOL);

        Provider[] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++)
            Security.addProvider(providers[i]);
    }

    public Algorithm(AlgorithmTypes hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
        try {
            if (bouncyCastleAlgorithms.contains(hashingAlgorithm))
                this.messageDigestAlgorithm = this.getBouncyCastle(hashingAlgorithm);
            else
                this.cryptoHashAlgorithm = this.getCryptoHash(hashingAlgorithm);
        } catch (Exception e) {
            log.error("error finding algorithm {} in cryptohash, Exception {}", hashingAlgorithm, e);
        }
    }

    @Override
    public byte[] hash(byte[] input) {
        if (bouncyCastleAlgorithms.contains(hashingAlgorithm))
            return messageDigestAlgorithm.digest(input);
        return cryptoHashAlgorithm.digest(input);
    }

    private java.security.MessageDigest getBouncyCastle(AlgorithmTypes hashingAlgorithm) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(hashingAlgorithm.toString().replace('_', '-'), new BouncyCastleProvider());
        return messageDigest;
    }

    private fr.cryptohash.Digest getCryptoHash(AlgorithmTypes hashingAlgorithm) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (fr.cryptohash.Digest) Class.forName("fr.cryptohash." + hashingAlgorithm.toString()).getDeclaredConstructor().newInstance();
    }
}
