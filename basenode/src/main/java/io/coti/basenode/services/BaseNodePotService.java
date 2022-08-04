package io.coti.basenode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.interfaces.IPotService;
import io.coti.pot.ProofOfTrust;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

@Slf4j
@Service
public class BaseNodePotService implements IPotService {

    @Value("${network.difficulty}")
    protected String difficulty;
    protected byte[] targetDifficulty;

    ConcurrentHashMap<Integer, ProofOfTrust> mapOfPots = new ConcurrentHashMap<Integer, ProofOfTrust>();

    public void init() {
        targetDifficulty = parseHexBinary(difficulty);
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public boolean validatePot(TransactionData transactionData) {

        ProofOfTrust pot =mapOfPots.get(transactionData.getRoundedSenderTrustScore());
        if (pot == null) {
                pot = new ProofOfTrust(transactionData.getRoundedSenderTrustScore());
                mapOfPots.put(transactionData.getRoundedSenderTrustScore(), pot);
        }
        return pot.verify(transactionData.getHash().
                getBytes(), transactionData.getNonces(), targetDifficulty);
    }
}
