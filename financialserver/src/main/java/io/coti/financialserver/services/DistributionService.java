package io.coti.financialserver.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.InitialFundDataHash;
import io.coti.basenode.model.Transactions;
import io.coti.financialserver.data.DistributionData;
import io.coti.financialserver.data.DistributionReleaseDateData;
import io.coti.financialserver.data.ReservedAddress;
import io.coti.financialserver.model.DistributionReleaseDates;
import io.coti.financialserver.model.Distributions;
import io.coti.financialserver.model.InitialFundsHashes;
import io.coti.financialserver.utils.DatesHelper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;

import java.math.BigDecimal;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

@Slf4j
@Service
public class DistributionService {

    private static final int COTI_GENESIS_ADDRESS_INDEX = 0;
    private static final String DISTRIBUTIONS_FILE_NAME = "initialDistributions.json";
    public static final int INITIAL_AMOUNT_FOR_TOKEN_SELL = 600000000;
    public static final int INITIAL_AMOUNT_FOR_INCENTIVES = 900000000;
    public static final int INITIAL_AMOUNT_FOR_TEAM = 300000000;
    public static final int INITIAL_AMOUNT_FOR_ADVISORS = 200000000;

    @Value("${financialserver.seed}")
    private String seed;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;
    @Autowired
    RollingReserveService rollingReserveService;
    @Autowired
    InitialFundsHashes initialFundsHashes;
    @Autowired
    TransactionCreationService transactionCreationService;
    @Autowired
    Distributions distributions;
    @Autowired
    DistributionReleaseDates distributionReleaseDates;
    @Autowired
    Transactions transactions;

    public void distributeToInitialFunds() {
        Hash cotiGenesisAddress = CryptoHelper.generateAddress(seed, COTI_GENESIS_ADDRESS_INDEX);
        EnumSet<ReservedAddress> initialFundDistributionAddresses = EnumSet.range(ReservedAddress.TOKEN_SELL,ReservedAddress.ADVISORS);
        initialFundDistributionAddresses.forEach(addressIndex -> {
            Hash fundAddress = CryptoHelper.generateAddress(seed, Math.toIntExact(addressIndex.getIndex()));

            if(!isInitialTransactionExistsByAddress(fundAddress))
            {
                BigDecimal amount = getInitialAmountByAddressIndex(addressIndex);
                transactionCreationService.createInitialTransactionToFund(amount, cotiGenesisAddress, fundAddress);
                InitialFundDataHash initialFundDataHashElement = new InitialFundDataHash(fundAddress);
                initialFundsHashes.put(initialFundDataHashElement);
            }
        });
    }

    private boolean isInitialTransactionExistsByAddress(Hash fundAddress) {
        // Verify if transaction hash is not already in new table for initial transactions
        return ( initialFundsHashes != null && initialFundsHashes.getByHash(fundAddress) != null);
    }

    private BigDecimal getInitialAmountByAddressIndex(ReservedAddress addressIndex) {
        BigDecimal amount = BigDecimal.ZERO;
        if(addressIndex.isInitialFundDistribution()) {
            switch(addressIndex)
            {
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

    public void startLoadDistributionsFromJsonFileThread() {
        Thread distributeFromInitialFundsThread = new Thread(this::loadDistributionsFromJsonFile);
        distributeFromInitialFundsThread.start();
    }

    public void loadDistributionsFromJsonFile() {
            try {
                List<DistributionData> distributionsDataList = getDistributionsDataFromJsonFile();
                distributionsDataList.forEach(distributionData -> distributions.put(distributionData));
            } catch (Exception e) {
                log.error("Distribute from initial funds exception", e);
            }
    }

    private List<DistributionData> getDistributionsDataFromJsonFile() throws Exception {

        String distributionsJsonStr = new String(Files.readAllBytes(Paths.get(DISTRIBUTIONS_FILE_NAME)));
        JSONArray distributionsJsonArray = new JSONArray(distributionsJsonStr);

        List<DistributionData> distributionDataList = new ArrayList<>();
        int distributionsJsonArrayLength = distributionsJsonArray.length();
        for( int i = 0; i < distributionsJsonArrayLength; i++) {

            DistributionData distributionData = new DistributionData((JSONObject) distributionsJsonArray.get(i));
            distributionData.setKycHash(new Hash(kycServerPublicKey));

            Date releaseDate;
            if( distributionData.isOnHold() ) {
                // January 1, 1970, 00:00:00 GMT
                // Won't be released until date is changed
                releaseDate = new Date(0);
            }
            else {
                releaseDate = DatesHelper.getDateNumberOfDaysAfterToday(distributionData.getOnHoldDays());
            }

            Hash distributionReleaseDateHash = new Hash(releaseDate.getTime());
            DistributionReleaseDateData distributionReleaseDateData = distributionReleaseDates.getByHash(distributionReleaseDateHash);

            if (distributionReleaseDateData == null) {
                distributionReleaseDateData = new DistributionReleaseDateData(releaseDate);
                //TODO: Check if needed to update distributionReleaseDates with new value
//                distributionReleaseDates.put(distributionReleaseDateData);
            }
            distributionReleaseDateData.getDistributionHashesList().add(distributionData.getHash());
            distributionDataList.add(distributionData);
        }
        return distributionDataList;
    }


}
