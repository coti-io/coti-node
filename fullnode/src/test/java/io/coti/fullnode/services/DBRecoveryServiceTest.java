package io.coti.fullnode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.model.*;
import io.coti.basenode.services.BaseNodeDBRecoveryService;
import io.coti.basenode.services.interfaces.IAwsService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.fullnode.database.RocksDBConnector;
import io.coti.fullnode.model.ExplorerIndexes;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

@ContextConfiguration(classes = {BaseNodeDBRecoveryService.class, Addresses.class,  IDatabaseConnector.class, RocksDBConnector.class})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class DBRecoveryServiceTest {

    @Autowired
    private Addresses addresses;

    @Autowired
    public IDatabaseConnector databaseConnector;
    @Autowired
    private RocksDBConnector rocksDBConnector;

    @MockBean
    private IAwsService awsService;
    @MockBean
    private INetworkService networkService;
    @MockBean
    private RestTemplate restTemplate;
    @MockBean
    private HttpJacksonSerializer jacksonSerializer;
    @MockBean
    private Transactions transactions;
    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private TransactionVotes transactionVotes;
    @MockBean
    private NodeRegistrations nodeRegistrations;
    @MockBean
    private ExplorerIndexes explorerIndexes;
    @MockBean
    private RequestedAddressHashes requestedAddressHashes ;

    private static String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    @Test
    public void generateAddresses(){
        rocksDBConnector.setColumnFamily();
        databaseConnector.init();

        int addressesToGenerate = 20_000_000;
        log.info("Starting to generate {} addresses",addressesToGenerate);
        generateAndStoreAddressHashes(addressesToGenerate);
        log.info("Generated {} addresses",addressesToGenerate);
        Assert.assertTrue(true);
    }

    private void generateAndStoreAddressHashes(int addressesToGenerate){
        AddressData addressData;
        for (int i = 0; i <= addressesToGenerate ; i++){
            addressData = new AddressData(generateAddressHash());
            addresses.put(addressData);
        }
    }


    private Hash generateAddressHash(){
        StringBuilder hexa = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 16);
            hexa.append(hexaOptions[randomNum]);
        }
        String generatedPublicKey = hexa.toString();
        byte[] crc32ToAdd = getCrc32OfByteArray(DatatypeConverter.parseHexBinary(generatedPublicKey));
        return new Hash(generatedPublicKey + DatatypeConverter.printHexBinary(crc32ToAdd));
    }

    private static byte[] getCrc32OfByteArray(byte[] array) {
        Checksum checksum = new CRC32();

        byte[] addressWithoutPadding = CryptoHelper.removeLeadingZerosFromAddress(array);
        checksum.update(addressWithoutPadding, 0, addressWithoutPadding.length);
        byte[] checksumValue = ByteBuffer.allocate(4).putInt((int) checksum.getValue()).array();
        return checksumValue;
    }

    private void logWithTimeStart(String msg){
        log.info(" ******** Time: {}. start {}", Instant.now(), msg);
    }

    private void logWithTimeEnd(String msg){
        log.info(" ******** Time: {}. finish {}", Instant.now(), msg);
    }
}
