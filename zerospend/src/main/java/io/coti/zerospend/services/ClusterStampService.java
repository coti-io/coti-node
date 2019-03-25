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

    private void loadInitialClusterStamp() {

        String clusterstampFileLocation = "clusterstamp.csv";
        File clusterstampFile = new File(clusterstampFileLocation);
        ClusterStampData initialClusterStampData = new ClusterStampData();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(clusterstampFile))) {

            String line;
            String[] lineSplitted;
            String[] addressDetails;
            int lineNumber = 0;
            int fundId;
            String fundName;
            float fundPercentage;

            while ((line = bufferedReader.readLine()) != null) {

                line = line.trim();

                if(line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }

                lineNumber++;

                if(lineNumber <= NUMBER_OF_GENESIS_ADDRESSES) {
                    addressDetails = line.split(",");
                    if (addressDetails.length != 2) {
                        throw new Exception("Bad csv file format");
                    }
                    Hash addressHash = new Hash(addressDetails[0]);
                    BigDecimal addressAmount = new BigDecimal(addressDetails[1]);
                    initialClusterStampData.getBalanceMap().put(addressHash, addressAmount);
                }
                else {
                    lineSplitted = line.split(",");
                    if (lineSplitted.length == 3) {
                        try {
                            fundId = Integer.parseInt(lineSplitted[0]);
                            fundName = lineSplitted[1];
                            fundPercentage = Float.parseFloat(lineSplitted[2]) / 100;
                            initialClusterStampData.getInitialFundDataList().add(new InitialFundData(fundId, fundName, fundPercentage));
                        }
                        catch (NumberFormatException e) {
                            throw new Exception("Bad csv file format");
                        }
                    }
                    else {
                        throw new Exception("Bad csv file format");
                    }
                }
            }
            clusterStamps.put(initialClusterStampData);
            log.info("Initial clusterstamp is loaded");
        } catch (Exception e) {
            log.error("Errors on clusterstamp loading: {}", e);
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