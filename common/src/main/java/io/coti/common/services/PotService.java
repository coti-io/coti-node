package io.coti.common.services;
import coti.pot.ProofOfTransaction;
import io.coti.common.data.TransactionData;
import io.coti.common.services.interfaces.IPotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;


import static javax.xml.bind.DatatypeConverter.parseHexBinary;

@Slf4j
@Service
public class PotService implements IPotService {
    @Value("${network.difficulty}")
    protected String difficulty;
    protected static byte[] targetDifficulty;

    @PostConstruct
    public void init() {
        targetDifficulty = parseHexBinary(difficulty);
    }

    @Override
    public boolean validatePot(TransactionData transactionData) {
        ProofOfTransaction pot = new ProofOfTransaction(
                transactionData.getRoundedSenderTrustScore());
        boolean valid = pot.verify(transactionData.getHash().
                getBytes(), transactionData.getNonces(), targetDifficulty);
        return valid;
    }
}
