package io.coti.common.pow;

public interface IAlgorithm {
    enum AlgorithmTypes {
        BLAKE512, BMW512, Groestl512, JH512,
        Keccak512, Skein512, Luffa512, CubeHash512,
        SHAvite512, SIMD512, ECHO512, Hamsi512,
        Fugue512, Shabal512, Whirlpool, SHA512
    }

    byte[] hash(byte[] input);
}
