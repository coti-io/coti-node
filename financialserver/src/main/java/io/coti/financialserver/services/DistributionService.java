package io.coti.financialserver.services;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.InitialFundData;
import io.coti.financialserver.data.ReservedAddress;
import io.coti.financialserver.model.InitialFunds;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.EnumSet;

@Slf4j
@Service
public class DistributionService {

    private static final int COTI_GENESIS_ADDRESS_INDEX = Math.toIntExact(ReservedAddress.GENESIS_ONE.getIndex());
    public static final int INITIAL_AMOUNT_FOR_TOKEN_SALE = 600000000;
    public static final int INITIAL_AMOUNT_FOR_INCENTIVES = 900000000;
    public static final int INITIAL_AMOUNT_FOR_TEAM = 300000000;
    public static final int INITIAL_AMOUNT_FOR_ADVISORS = 200000000;
    @Value("${financialserver.seed}")
    private String seed;
    @Autowired
    private InitialFunds initialFunds;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    public void distributeToInitialFunds() {
        Hash cotiGenesisAddress = nodeCryptoHelper.generateAddress(seed, COTI_GENESIS_ADDRESS_INDEX);
        EnumSet<ReservedAddress> initialFundDistributionAddresses = ReservedAddress.getInitialFundDistributionAddresses();
        initialFundDistributionAddresses.forEach(addressIndex -> {
            Hash fundAddress = nodeCryptoHelper.generateAddress(seed, Math.toIntExact(addressIndex.getIndex()));

            if (!isInitialTransactionExistsByAddress(fundAddress)) {
                BigDecimal amount = getInitialAmountByAddressIndex(addressIndex);
                Hash initialTransactionHash;
                try {
                    initialTransactionHash = transactionCreationService.createInitialTransactionToFund(amount, cotiGenesisAddress, fundAddress, COTI_GENESIS_ADDRESS_INDEX);
                } catch (Exception e) {
                    log.error("Failed to create initial fund");
                    log.error("{}: {}", e.getClass().getName(), e.getMessage());
                    return;
                }
                InitialFundData initialFundDataElement = new InitialFundData(fundAddress, initialTransactionHash);
                initialFunds.put(initialFundDataElement);
            }
        });
    }

    private boolean isInitialTransactionExistsByAddress(Hash fundAddress) {
        return initialFunds.getByHash(fundAddress) != null;
    }

    private BigDecimal getInitialAmountByAddressIndex(ReservedAddress addressIndex) {
        BigDecimal amount = BigDecimal.ZERO;
        if (addressIndex.isInitialFundDistribution()) {
            switch (addressIndex) {
                case TOKEN_SALE:
                    amount = new BigDecimal(INITIAL_AMOUNT_FOR_TOKEN_SALE);
                    break;
                case INCENTIVES:
                    amount = new BigDecimal(INITIAL_AMOUNT_FOR_INCENTIVES);
                    break;
                case TEAM:
                    amount = new BigDecimal(INITIAL_AMOUNT_FOR_TEAM);
                    break;
                case ADVISORS:
                    amount = new BigDecimal(INITIAL_AMOUNT_FOR_ADVISORS);
                    break;
            }
        }
        return amount;
    }


}
