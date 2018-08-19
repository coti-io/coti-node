package io.coti.common.services;

import coti.crypto.AlphaNetProofOfWork;
import io.coti.common.crypto.TransactionCrypto;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.services.interfaces.IPoftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

@Service
public class PoftService implements IPoftService {

    private static final byte[] targetDifficulty = parseHexBinary("00F000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000");
//  private static final byte[] targetDifficulty = parseHexBinary("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF" +
  // "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
//  private static final byte[] targetDifficulty = parseHexBinary("01F000000000000000000000000000000000" +
//          "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
    private static final byte[] startOrderSeed = parseHexBinary("0123456789abcdef");
//    private static final byte[] startOrderSeed = parseHexBinary("0000000000000000");


    @Autowired
    private TransactionCrypto transactionCrypto;

    @PostConstruct
    private void init(){
//        hash2poft = new ConcurrentHashMap<>();
    }

    @Override
    public void poftAction(TransactionData transactionData) {
        AlphaNetProofOfWork poft = new AlphaNetProofOfWork(startOrderSeed,
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
