package coti.crypto;

public interface IAlgorithm {
    enum AlgorithmTypes {
        SHA512,
        Keccak512,
        BMW512,
        BLAKE512,
        Skein512,
        Shabal512,
        Hamsi512,
        Luffa512,
        SHAvite512,
        JH512,
        Whirlpool,
        CubeHash512,
        Fugue512,
        Groestl512,
        ECHO512,
        SIMD512,
    }

    byte[] hash(byte[] input);
}
