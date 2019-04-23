package io.coti.financialserver.services;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.InitialFundData;
import io.coti.basenode.model.Transactions;
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
    public static final int INITIAL_AMOUNT_FOR_TOKEN_SELL = 600000000;
    public static final int INITIAL_AMOUNT_FOR_INCENTIVES = 900000000;
    public static final int INITIAL_AMOUNT_FOR_TEAM = 300000000;
    public static final int INITIAL_AMOUNT_FOR_ADVISORS = 200000000;

    @Value("${financialserver.seed}")
    private String seed;
    @Autowired
    RollingReserveService rollingReserveService;
    @Autowired
    InitialFunds initialFunds;
    @Autowired
    TransactionCreationService transactionCreationService;
    @Autowired
    Transactions transactions;
    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    public void distributeToInitialFunds() {
        Hash cotiGenesisAddress = nodeCryptoHelper.generateAddress(seed, COTI_GENESIS_ADDRESS_INDEX);
        EnumSet<ReservedAddress> initialFundDistributionAddresses = ReservedAddress.getInitialFundDistributionAddresses();
        initialFundDistributionAddresses.forEach(addressIndex -> {
            Hash fundAddress = nodeCryptoHelper.generateAddress(seed, Math.toIntExact(addressIndex.getIndex()));

            if (!isInitialTransactionExistsByAddress(fundAddress)) {
                BigDecimal amount = getInitialAmountByAddressIndex(addressIndex);
                Hash initialTransactionHash = transactionCreationService.createInitialTransactionToFund(amount, cotiGenesisAddress, fundAddress, COTI_GENESIS_ADDRESS_INDEX);
                InitialFundData initialFundDataElement = new InitialFundData(fundAddress, initialTransactionHash);
                initialFunds.put(initialFundDataElement);
            }
        });
    }

    private boolean isInitialTransactionExistsByAddress(Hash fundAddress) {
        // Verify if transaction hash is not already in new table for initial transactions
        return initialFunds.getByHash(fundAddress) != null;
    }

    private BigDecimal getInitialAmountByAddressIndex(ReservedAddress addressIndex) {
        BigDecimal amount = BigDecimal.ZERO;
        if (addressIndex.isInitialFundDistribution()) {
            switch (addressIndex) {
                case TOKEN_SELL:
                    amount = new BigDecimal(INITIAL_AMOUNT_FOR_TOKEN_SELL);
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
