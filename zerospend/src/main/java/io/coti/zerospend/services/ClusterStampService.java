package io.coti.zerospend.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;

/**
 * A service that provides Cluster Stamp functionality for Zero Spend node.
 */
@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    private static final int NUMBER_OF_GENESIS_ADDRESSES = 2; // Genesis, and Rolling Reserve
    public static final int NUMBER_OF_ADDRESS_LINE_DETAILS = 2;
    public static final int ADDRESS_DETAILS_HASH_PLACEMENT = 0;
    public static final int ADDRESS_DETAILS_AMOUNT_PLACEMENT = 1;
    public static final int NUMBER_OF_FUNDS_LINE_DETAILS = 3;
    public static final int FUNDS_DETAILS_FUND_ID_PLACEMENT  = 0;
    public static final int FUNDS_DETAILS_FUND_NAME_PLACEMENT  = 1;
    public static final int FUNDS_DETAILS_FUND_PERCENTAGE_PLACEMENT  = 2;
    public static final String BAD_CSV_FILE_FORMAT = "Bad csv file format";

    private void loadInitialClusterStamp() {

        String clusterstampFileLocation = "clusterstamp.csv";  //TODO: consider changing to property
        File clusterstampFile = new File(clusterstampFileLocation);
        ClusterStampData initialClusterStampData = new ClusterStampData();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(clusterstampFile))) {

            String line;
            String[] lineSplit;
            int lineNumber = 0;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if(line.isEmpty() || line.charAt(0) == '#') { continue; }

                lineNumber++;
                if(lineNumber <= NUMBER_OF_GENESIS_ADDRESSES)
                {
                    fillGenesisAddressesInitialClusterStampDataFromLine(initialClusterStampData, line);
                } else {
                    lineSplit = line.split(",");
                    if (lineSplit.length == NUMBER_OF_FUNDS_LINE_DETAILS)
                    {
                        fillFundInitialClusterStampDataFromLine(initialClusterStampData, lineSplit);
                    } else {
                        throw new Exception(BAD_CSV_FILE_FORMAT);
                    }
                }
            }
            clusterStamps.put(initialClusterStampData);
            log.info("Initial clusterstamp is loaded");
        } catch (Exception e) {
            log.error("Errors on clusterstamp loading: {}", e);
        }
    }

    private void fillGenesisAddressesInitialClusterStampDataFromLine(ClusterStampData initialClusterStampData, String line) throws Exception {
        String[] addressDetails;
        addressDetails = line.split(",");
        if (addressDetails.length != NUMBER_OF_ADDRESS_LINE_DETAILS) {
            throw new Exception(BAD_CSV_FILE_FORMAT);
        }
        Hash addressHash = new Hash(addressDetails[ADDRESS_DETAILS_HASH_PLACEMENT]);
        BigDecimal addressAmount = new BigDecimal(addressDetails[ADDRESS_DETAILS_AMOUNT_PLACEMENT]);
        initialClusterStampData.getBalanceMap().put(addressHash, addressAmount);
    }

    private void fillFundInitialClusterStampDataFromLine(ClusterStampData initialClusterStampData, String[] lineSplit) throws Exception {
        int fundId;
        String fundName;
        float fundPercentage;
        try {
            fundId = Integer.parseInt(lineSplit[FUNDS_DETAILS_FUND_ID_PLACEMENT]);
            fundName = lineSplit[FUNDS_DETAILS_FUND_NAME_PLACEMENT];
            fundPercentage = Float.parseFloat(lineSplit[FUNDS_DETAILS_FUND_PERCENTAGE_PLACEMENT]) / 100;
            initialClusterStampData.getInitialFundDataList().add(new InitialFundData(fundId, fundName, fundPercentage));
        }
        catch (NumberFormatException e) {
            throw new Exception(BAD_CSV_FILE_FORMAT);
        }
    }

    @Override
    protected ClusterStampData getLastClusterStamp() {
        ClusterStampData localClusterStampData = clusterStamps.getByHash(LAST_CLUSTER_STAMP_HASH);
        if(localClusterStampData == null) {
            loadInitialClusterStamp();
            localClusterStampData = clusterStamps.getByHash(LAST_CLUSTER_STAMP_HASH);
        }

        clusterStampCrypto.signMessage(localClusterStampData);
        return localClusterStampData;
    }
}