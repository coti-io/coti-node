package io.coti.common.services;

import coti.crypto.AlphaNetProofOfWork;
import io.coti.common.crypto.TransactionCrypto;
import io.coti.common.data.TransactionData;
import io.coti.common.services.interfaces.IPoftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

@Service
public class PoftService implements IPoftService {

    private static final byte[] targetDifficulty = parseHexBinary("00F000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000");
    private static final byte[] startOrderSeed = parseHexBinary("0123456789abcdef");
    @Autowired
    private TransactionCrypto transactionCrypto;

    @Override
    public void poftAction(TransactionData transactionData) {
        AlphaNetProofOfWork poft = new AlphaNetProofOfWork(parseHexBinary("0123456789abcdef"),
                (int) Math.round(transactionData.getTrustChainTrustScore()));  // setup
        int[] nonces = poft.hash(transactionData.getHash().getBytes(), targetDifficulty); //calc
        transactionData.setNonces(nonces);
    }

    @Override
    public boolean validatePoft(TransactionData transactionData) {
        AlphaNetProofOfWork poft = new AlphaNetProofOfWork(startOrderSeed,
                (int) Math.round(transactionData.getTrustChainTrustScore()));
        boolean valid = poft.verify(transactionData.getHash().
                getBytes(), transactionData.getNonces(), targetDifficulty);        // verify - o(1)
        return valid;
    }
}
