package io.coti.pot.interfaces;

public interface IAlgorithm {
    enum AlgorithmType {
        SKEIN_512_512("Skein-512-512"),
        BMW_512("BMW512"),
        BLAKE2B_512("BLAKE2B-512"),
        SHA_512("SHA-512"),
        ECHO_512("ECHO512"),
        KECCAK_512("KECCAK-512"),
        SHABAL_512("Shabal512"),
        JH_512("JH512"),
        WHIRLPOOL("WHIRLPOOL"),
        CUBE_HASH_512("CubeHash512"),
        SHA_VITE_512("SHAvite512"),
        LUFFA_512("Luffa512"),
        SIMD_512("SIMD512"),
        FUGUE_512("Fugue512"),
        GROESTL_512("Groestl512"),
        HAMSI_512("Hamsi512");

        private final String algorithmName;

        AlgorithmType(String algorithmName) {
            this.algorithmName = algorithmName;
        }

        @Override
        public String toString() {
            return algorithmName;
        }
    }

    byte[] hash(byte[] input);
}
