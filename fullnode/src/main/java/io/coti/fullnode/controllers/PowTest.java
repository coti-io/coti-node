package io.coti.fullnode.controllers;

import coti.crypto.AlphaNetProofOfWork;
import io.coti.common.crypto.BaseTransactionWithPrivateKey;
import io.coti.common.crypto.TransactionCrypto;
import io.coti.common.crypto.TransactionCyptoCreator;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.TransactionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

@RestController
@RequestMapping("/testpow")
public class PowTest {

    private String hexPrivateKey = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f83";
    private byte[] targetDifficulty = parseHexBinary("00F000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000");


    @Autowired
    private TransactionCrypto transactionCrypto;

    @RequestMapping(method = RequestMethod.GET)
    public String powTestController(){
        ArrayList<BaseTransactionData> bxDataList = new ArrayList<>();
        bxDataList.add(new BaseTransactionWithPrivateKey(new BigDecimal(-10), new Date(), hexPrivateKey));
        TransactionData transactionData = new TransactionData(bxDataList, "test", 80.53, new Date());
        AlphaNetProofOfWork pow = new AlphaNetProofOfWork("abcdefghijk".getBytes(), 0);  // setup

        TransactionCyptoCreator txCreator = new TransactionCyptoCreator(transactionData);
        txCreator.signTransaction();
        transactionCrypto.signMessage(transactionData);
        int[] nonces = pow.hash(transactionData.getHash().getBytes(), targetDifficulty); //calc
        boolean valid = pow.verify(transactionData.getHash().getBytes(), nonces, targetDifficulty); // verify - o(1)

        return String.valueOf(valid);
    }

    /*
        private String hexPrivateKey = "1731ceb7b1d3a9c78d6a3009ca7021569eeb6a4ece86f0b744afbc3fabf82f83";
    private byte[] targetDifficulty = parseHexBinary("00F000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000000000000000000000000000000");


    @Autowired
    private TransactionCrypto transactionCrypto;
    @Test
    public void testPow(){
        ArrayList<BaseTransactionData> bxDataList = new ArrayList<>();
        bxDataList.add(new BaseTransactionWithPrivateKey(new BigDecimal(-10), new Date(), hexPrivateKey));
        TransactionData transactionData = new TransactionData(bxDataList, "test", 80.53, new Date());
        AlphaNetProofOfWork pow = new AlphaNetProofOfWork("abcdefghijk".getBytes(), 0);  // setup

        TransactionCyptoCreator txCreator = new TransactionCyptoCreator(transactionData);
        txCreator.signTransaction();
        transactionCrypto.signMessage(transactionData);
        int[] nonces = pow.hash(transactionData.getHash().getBytes(), targetDifficulty); //calc
        boolean valid = pow.verify(transactionData.getHash().getBytes(), nonces, targetDifficulty); // verify - o(1)

        Assert.assertTrue(valid);

    }

     */
}
