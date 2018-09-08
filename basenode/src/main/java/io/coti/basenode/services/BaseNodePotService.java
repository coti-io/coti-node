package io.coti.basenode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.interfaces.IPotService;
import io.coti.pot.ProofOfTrust;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

@Slf4j
@Service
public class BaseNodePotService implements IPotService {
    @Value("${network.difficulty}")
    protected String difficulty;
    protected static byte[] targetDifficulty;

    public void init() {
        targetDifficulty = parseHexBinary(difficulty);
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public boolean validatePot(TransactionData transactionData) {
        if (transactionData.isZeroSpend()) {
            return true;
        }
        ProofOfTrust pot = new ProofOfTrust(
                transactionData.getRoundedSenderTrustScore());
        boolean valid = pot.verify(transactionData.getHash().
                getBytes(), transactionData.getNonces(), targetDifficulty);
        return valid;
    }
}
