package coti.crypto;

import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.digests.*;

public class AlphaNetAlgorithm implements IAlgorithm {

    private AlgorithmTypes hashingAlgorithm;

    private fr.cryptohash.Digest cryptoHashAlgorithm;
    private MessageDigest messageDigestAlgorithm;
    private org.bouncycastle.crypto.Digest bouncyCastleAlgorithm;

    public AlphaNetAlgorithm(AlgorithmTypes hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;

        switch (hashingAlgorithm) {
            case SHA512: {
                this.messageDigestAlgorithm = this.getSHA512();
                break;
            }
            case BLAKE512:
            case Keccak512:
            case Skein512:
            case Whirlpool: {
                this.bouncyCastleAlgorithm = this.getBouncyCastle(hashingAlgorithm);
                break;
            }
            default: {
                this.cryptoHashAlgorithm = this.getCryptoHash(hashingAlgorithm);
                break;
            }
        }
    }

    @Override
    public byte[] hash(byte[] input) {
        switch (hashingAlgorithm) {
            case SHA512: {
                return messageDigestAlgorithm.digest(input);
            }
            case BLAKE512:
            case Keccak512:
            case Skein512:
            case Whirlpool: {
                bouncyCastleAlgorithm.update(input, 0, input.length);
                byte[] out = new byte[bouncyCastleAlgorithm.getDigestSize()];
                bouncyCastleAlgorithm.doFinal(out, 0);
                return out;
            }
            default: {
                return cryptoHashAlgorithm.digest(input);
            }
        }
    }

    private MessageDigest getSHA512() {
        try {
            return MessageDigest.getInstance("SHA-512");
        }
        catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }

    private org.bouncycastle.crypto.Digest getBouncyCastle(AlgorithmTypes hashingAlgorithm) {
        switch (hashingAlgorithm) {
            case BLAKE512: {
                return new Blake2bDigest(512);
            }
            case Keccak512: {
                return new KeccakDigest(512);
            }
            case Skein512: {
                return new SkeinDigest(512, 512);
            }
            case Whirlpool: {
                return new WhirlpoolDigest();
            }
        }
        return null;
    }

    private fr.cryptohash.Digest getCryptoHash(AlgorithmTypes hashingAlgorithm) {
        try {
            return (fr.cryptohash.Digest)Class.forName("fr.cryptohash." + hashingAlgorithm.toString()).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
