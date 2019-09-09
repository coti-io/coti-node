package io.coti.trustscore.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.trustscore.data.UnlinkedAddressData;
import io.coti.trustscore.model.AddressUserIndex;
import io.coti.trustscore.model.UnlinkedAddresses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@Slf4j
public class ClusterStampService extends BaseNodeClusterStampService {
    @Autowired
    private AddressUserIndex addressUserIndex;

    @Autowired
    private UnlinkedAddresses unlinkedAddresses;

    // todo there should be a date
    @Override
    public void continueUpdateBalanceFromClusterStamp(Hash addressHash, BigDecimal addressAmount) {
        if (unlinkedAddresses.getByHash(addressHash) == null && addressUserIndex.getByHash(addressHash) == null) {
            UnlinkedAddressData unlinkedAddressData = new UnlinkedAddressData(addressHash);
            unlinkedAddressData.insertToDateToBalanceMap(LocalDate.now(ZoneOffset.UTC), addressAmount.doubleValue());
            unlinkedAddresses.put(unlinkedAddressData);
        }
    }
}
