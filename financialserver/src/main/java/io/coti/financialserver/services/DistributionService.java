package io.coti.financialserver.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.InitialFundData;
import io.coti.basenode.model.Transactions;
import io.coti.financialserver.data.DistributionData;
import io.coti.financialserver.data.DistributionReleaseDateData;
import io.coti.financialserver.model.DistributionReleaseDates;
import io.coti.financialserver.model.Distributions;
import io.coti.financialserver.model.InitialFunds;
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
import java.util.List;

@Slf4j
@Service
public class DistributionService {

    private static final int COTI_GENESIS_ADDRESS_INDEX = 0;
    private static final String DISTRIBUTIONS_FILE_NAME = "distributions.json";
    public static final int COIN_GENESIS_MAX_AMOUNT = 2000000000;

    @Value("${financialserver.seed}")
    private String seed;
    @Value("${kycserver.public.key}")
    private String kycServerPublicKey;
    @Autowired
    RollingReserveService rollingReserveService;
    @Autowired
    InitialFunds initialFunds;
    @Autowired
    TransactionCreationService transactionCreationService;
    @Autowired
    Distributions distributions;
    @Autowired
    DistributionReleaseDates distributionReleaseDates;
    @Autowired
    Transactions transactions;

    public void distributeToInitialFunds(List<InitialFundData> initialFundDataList) {

        Hash cotiGenesisAddress = CryptoHelper.generateAddress(seed, COTI_GENESIS_ADDRESS_INDEX);
        verifySumOfFundsPercentages(initialFundDataList);

        initialFundDataList.forEach(initialFundData -> {

            initialFundData.setAddressIndex(rollingReserveService.getNextAddressIndex());
            initialFunds.put(initialFundData);

            Float floatFundAmount = (initialFundData.getFundPercentage() / 100) * COIN_GENESIS_MAX_AMOUNT;
            BigDecimal amount = new BigDecimal(floatFundAmount.toString());
            Hash fundAddress = CryptoHelper.generateAddress(seed, initialFundData.getAddressIndex());
            transactionCreationService.createInitialTransactionToFund(amount, cotiGenesisAddress, fundAddress);
        });
    }

    public void verifySumOfFundsPercentages(List<InitialFundData> initialFundDataList) {
        //TODO: Consider changing logic if exact sum of 100 is needed and in case distribution should be prevented, change to boolean
        Float fundsPercentagesSum = Float.valueOf(0);
        for (InitialFundData fundData : initialFundDataList) {
            fundsPercentagesSum += fundData.getFundPercentage();
        }
        //TODO: consider throwing error
        if( fundsPercentagesSum > 100)
            log.error("Distribution percentages sum: {} from initial funds exceed 100", fundsPercentagesSum);
    }

    public void startLoadDistributionsFromJsonFileThread() {
        Thread distributeFromInitialFundsThread = new Thread(() -> loadDistributionsFromJsonFile());
        distributeFromInitialFundsThread.start();
    }

    public void loadDistributionsFromJsonFile() {

        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<DistributionData> distributionsDataList = getDistributionsDataFromJsonFile();
                distributionsDataList.forEach(distributionData -> distributions.put(distributionData));
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("Distribute from initial funds exception", e);
                Thread.currentThread().interrupt();
            }
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
                distributionReleaseDates.put(distributionReleaseDateData);
            }

            distributionReleaseDateData.getDistributionHashesList().add(distributionData.getHash());

            distributionDataList.add(distributionData);
        }

        return distributionDataList;
    }
}
