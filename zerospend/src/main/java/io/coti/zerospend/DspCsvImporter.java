package io.coti.zerospend;

import io.coti.common.data.Hash;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

@Component
@Slf4j
public class DspCsvImporter {

    private HashMap<String, Hash> host2NodeHashMap;

    @PostConstruct
    private void init() throws Exception {
        String snapshotFileLocation = "DSPs.csv";
        File snapshotFile = new File(snapshotFileLocation);
        host2NodeHashMap = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(snapshotFile))) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] nodeDetails = line.split(",");
                if (nodeDetails.length != 2) {
                    throw new Exception("Bad csv file format");
                }
                Hash dspNodeHash = new Hash(nodeDetails[1]);
                String host = nodeDetails[0];
                log.debug("The addressHash {} was loaded from the snapshot belongs to host {}", dspNodeHash, host);

                if (host2NodeHashMap.containsKey(host)) {
                    log.error("The host {} was already found in the snapshot", host);
                    throw new Exception(String.format("The address %s was already found in the snapshot", dspNodeHash));
                }
                host2NodeHashMap.put(host, dspNodeHash);
                log.debug("Loading from snapshot into inMem host2NodeHashMap host {} and dspNodeHash {}",
                        host, dspNodeHash);
            }
            log.info("host2NodeHashMap loading is finished");
        } catch (Exception e) {
            log.error("Errors on snapshot loading: {}", e);
            throw e;
        }
    }

    public HashMap<String, Hash> getHost2NodeHashMap() {
        return host2NodeHashMap;
    }

}
