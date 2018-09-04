package io.coti.pot;

import io.coti.pot.interfaces.IAlgorithm;
import io.coti.pot.interfaces.IAlgorithmOrder;
import io.coti.pot.interfaces.IAlgorithmWorker;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.nio.ByteBuffer;

class AlgorithmWorker implements IAlgorithmWorker {

    private IAlgorithmOrder ordering;

    public AlgorithmWorker(IAlgorithmOrder ordering) {
        this.ordering = ordering;
    }

    @Override
    public int[] hash(byte[] data, byte[] target) {
        BigInteger targetOutput = new BigInteger(1, target);
        int[] nonces = new int[ordering.getHashingAlgorithms().size()];
        byte[] lastCorrectHash = data;

        for (int i = 0; i < nonces.length; i++) {
            IAlgorithm.AlgorithmTypes hashingAlgorithm = ordering.getHashingAlgorithms().get(i);
            int nonce = findNonce(hashingAlgorithm, lastCorrectHash, targetOutput);
            lastCorrectHash = concatAndHash(hashingAlgorithm, lastCorrectHash, nonce);
            nonces[i] = nonce;
        }
        return nonces;
    }

    @Override
    public boolean verify(byte[] data, int[] nonce, byte[] target) {
        if (nonce.length != ordering.getHashingAlgorithms().size()) {
            return false;
        }

        BigInteger targetOutput = new BigInteger(1, target);
        byte[] lastCorrectHash = data;

        for (int i = 0; i < nonce.length; i++) {
            IAlgorithm.AlgorithmTypes hashingAlgorithm = ordering.getHashingAlgorithms().get(i);
            byte[] hashedData = concatAndHash(hashingAlgorithm, lastCorrectHash, nonce[i]);
            BigInteger currentOutput = new BigInteger(1, hashedData);
            if (currentOutput.compareTo(targetOutput) != -1) {
                return false;
            }
            lastCorrectHash = hashedData;
        }
        return true;
    }

    private int findNonce(IAlgorithm.AlgorithmTypes hashingAlgorithm, byte[] lastCorrectHash, BigInteger targetOutput) {
        byte[] hashedData;
        boolean validHash;
        int nonce = 0;

        do {
            hashedData = concatAndHash(hashingAlgorithm, lastCorrectHash, nonce);
            BigInteger currentOutput = new BigInteger(1, hashedData);
            validHash = (currentOutput.compareTo(targetOutput) == -1);
            nonce = nonce + 1;
        } while (!validHash);
        return nonce - 1;
    }

    private byte[] concatAndHash(IAlgorithm.AlgorithmTypes hashingAlgorithm, byte[] data, int nonce) {
        byte[] nonceByte = ByteBuffer.allocate(4).putInt(nonce).array();
        data = ArrayUtils.addAll(data, nonceByte);
        data = ordering.getHashingAlgorithm(hashingAlgorithm).hash(data);
        return data;
    }
}
