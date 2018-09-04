package io.coti.pot.interfaces;

public interface IAlgorithm {
    enum AlgorithmTypes {
        Skein_512_512,
        BMW512,
        BLAKE2B_512,
        SHA_512,
        ECHO512,
        KECCAK_512,
        Shabal512,
        JH512,
        WHIRLPOOL,
        CubeHash512,
        SHAvite512,
        Luffa512,
        SIMD512,
        Fugue512,
        Groestl512,
        Hamsi512,
    }

    byte[] hash(byte[] input);
}
